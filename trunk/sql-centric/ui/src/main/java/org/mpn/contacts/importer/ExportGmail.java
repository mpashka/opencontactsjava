/*
 * Copyright (c) 2005-2008 jNetX.
 * http://www.jnetx.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * jNetX. You shall not disclose such Confidential Information and
 * shall use it only in accordance with the terms of the license
 * agreement you entered into with jNetX.
 *
 * $Id$
 */
package org.mpn.contacts.importer;

import org.apache.log4j.Logger;
import org.mpn.contacts.framework.db.Row;
import org.mpn.contacts.ui.Data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * todo [!] Create javadocs for org.mpn.contacts.importer.ExportGmail here
 *
 * @author <a href="mailto:pmoukhataev@jnetx.ru">Pavel Moukhataev</a>
 * @version $Revision$
 */
public class ExportGmail {

    static final Logger log = Logger.getLogger("slee.ExportGmail");

    private String HEADER_NAME = "Name";
    private String HEADER_EMAIL = "E-mail";
    private String HEADER_NOTES = "Notes";

    private String SECTION_HEADER_NAME = "Section";
    private String SECTION_HEADER_VALUE = "Description";

    public String FIELD_NAME_MOBILE = "Mobile";
    public String FIELD_NAME_EMAIL = "Email";
    public String FIELD_NAME_PHONE = "Phone";
    public String FIELD_NAME_IM = "IM";
    public String FIELD_NAME_COMPANY = "Company";
    public String FIELD_NAME_COMPANY_POSITION = "Title";
    public String FIELD_NAME_ADDRESS = "Address";
    public String FIELD_NAME_NOTES = "Other";

    private Set<String> sectionNames = new HashSet<String>();

    private List<ContactInfo> contacts = new ArrayList<ContactInfo>();

    private static final Map<String, String> IM_TYPES = new HashMap<String, String>();

    static {
        IM_TYPES.put(Data.IM_TYPE_ICQ, "ICQ");
        IM_TYPES.put(Data.IM_TYPE_JABBER, "JABBER");
        IM_TYPES.put(Data.IM_TYPE_SKYPE, "SKYPE");
        IM_TYPES.put(Data.IM_TYPE_MSN, "MSN");
    }

    public void doExportGmail(File outFile) throws IOException {
        PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(outFile), "UTF-16"));
        saveExportHeader(out);


        final Map<Long, String> organizationNames = new HashMap<Long, String>();
        for (Row row : Data.organizationTable) {
            organizationNames.put(row.getData(Data.organizationTable.id), row.getData(Data.organizationName));
        }


        for (Row personRow : Data.personTable) {
            exportPerson(out, organizationNames, personRow);
        }

        out.close();
    }

    public void doExportGmail(File outPath, int limit) throws IOException {
        final Map<Long, String> organizationNames = new HashMap<Long, String>();
        for (Row row : Data.organizationTable) {
            organizationNames.put(row.getData(Data.organizationTable.id), row.getData(Data.organizationName));
        }

        PrintWriter out = null;
        int count = 0;
        int fileCount = 0;
        for (Row personRow : Data.personTable) {
            if (out == null || count > limit) {
                if (out != null) {
                    out.close();
                }
                fileCount++;
                count = 0;
                out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(outPath, "gmail-" + fileCount + ".csv")), "UTF-16"));
                saveExportHeader(out);
            }
            if (exportPerson(out, organizationNames, personRow)) {
                count++;
            }
        }
        out.close();
    }

    private void saveExportHeader(PrintWriter out) {
        out.println(HEADER_NAME + "," + HEADER_EMAIL + ","
                + SECTION_HEADER_NAME + " 1 - " + SECTION_HEADER_VALUE + ","
                + SECTION_HEADER_NAME + " 1 - " + FIELD_NAME_MOBILE + ","
                + SECTION_HEADER_NAME + " 1 - " + FIELD_NAME_EMAIL + ","
                + SECTION_HEADER_NAME + " 1 - " + FIELD_NAME_IM + ","
                + SECTION_HEADER_NAME + " 2 - " + SECTION_HEADER_VALUE + ","
                + SECTION_HEADER_NAME + " 2 - " + FIELD_NAME_PHONE + ","
                + SECTION_HEADER_NAME + " 2 - " + FIELD_NAME_EMAIL + ","
                + SECTION_HEADER_NAME + " 2 - " + FIELD_NAME_COMPANY + ","
                + SECTION_HEADER_NAME + " 2 - " + FIELD_NAME_COMPANY_POSITION
        );
    }

    private boolean exportPerson(PrintWriter out, Map<Long, String> organizationNames, Row personRow) {
        boolean artezio = false;
        Set<String> phones = new HashSet<String>();
        Set<String> ims = new HashSet<String>();
        Set<String> emails = new HashSet<String>();
//            Set<String> icqs = new HashSet<String>();
        String email = null;
        String companyEmail = null;
        String company = null;
        String companyPosition = null;
        String icq = null;

        Long personId = personRow.getData(Data.personTable.id);
        for (Row personMessagingRow : Data.personMessagingTable) {
            if (!personMessagingRow.getData(Data.personTable.id).equals(personId)) continue;
            String type = personMessagingRow.getData(Data.personMessagingType);
            String id = personMessagingRow.getData(Data.personMessagingId);
            if (type.equals(Data.IM_TYPE_EMAIL)) {
                if (id.startsWith("pmoukhataev") || id.startsWith("pavel.moukhataev")) return false;
                if (id.contains("@vc.") || id.contains("datastations") || id.contains("valuecommerce") || id.contains("looksmart")) {
                    // VC
                } else if (id.contains("artezio")) {
                    artezio = true;
                    company = "Artezio";
                    companyEmail = id;
                } else if (id.contains("jnetx")) {
                    companyEmail = id;
                } else {
                    if (email == null) {
                        email = id;
                    } else {
                        emails.add(id);
                    }
                }
            } else if (type.equals(Data.IM_TYPE_MOBILE)) {
                phones.add(id);
            } else {
                if (type.equals(Data.IM_TYPE_ICQ)) {
//                        icqs.add(Data.IM_TYPE_ICQ);
                    emails.add(id + "@icq.highsecure.ru");
                    icq = id;
                }
                String imType = IM_TYPES.get(type);
                if (imType == null) {
                    log.error("IM TYPE not found : " + type);
                }
                ims.add(imType + ": " + id);
            }
        }

        String companyPhone = null;
        if (!artezio) {
            for (Row personOrganizationRow : Data.personOrganizationTable) {
                if (personOrganizationRow.getData(Data.personTable.id).equals(personId)) {
                    company = organizationNames.get(personOrganizationRow.getData(Data.organizationTable.id));
                    String companyDepartment = personOrganizationRow.getData(Data.personOrganizationDepartment);
                    if (companyDepartment != null) {
                        company = company + " (" + companyDepartment + ")";
                    }
                    companyPosition = personOrganizationRow.getData(Data.personOrganizationPosition);
                    companyPhone = personOrganizationRow.getData(Data.phone);
                    break;
                }
            }
        }

        //
        StringBuilder name = new StringBuilder();
        appendString(name, " ", personRow.getData(Data.personFirstName));
        appendString(name, " ", personRow.getData(Data.personMiddleName));
        appendString(name, " ", personRow.getData(Data.personLastName));

        if (email == null && emails.isEmpty()) {
//                log.warn("Skip " + name);
            return false;
        }
        if (name.length() == 0) {
            if (icq != null) name.append(icq);
            else if (email != null) name.append(email);
        }


        StringBuilder emailsString = new StringBuilder();
        appendString(emailsString, ";", emails);

        StringBuilder imsString = new StringBuilder();
        appendString(imsString, ";", ims);

        StringBuilder mobilesString = new StringBuilder();
        appendString(mobilesString , ";", phones);


        saveString(out, name.toString(), email
                , "Personal", mobilesString.toString(), emailsString.toString(), imsString.toString()
                , "Work", companyPhone, companyEmail, company, companyPosition
        );
        out.println();
        return true;
    }

    static void saveString(PrintWriter out, String... strs) {
        for (int i = 0; i < strs.length; i++) {
            String str = strs[i];
            if (i > 0) {
                out.print(',');
            }
            if (str == null || str.length() == 0) continue;
            if (str.matches(".*[\\\",\\n\\r]+.*")) {
                str = '"' + str.replaceAll("\\\"", "\"\"") + '"';
            }
            out.print(str);
        }
    }


    static void appendString(StringBuilder value, String delimiter, String ... values) {
        appendString(value, delimiter, Arrays.asList(values));
    }

    static void appendString(StringBuilder value, String delimiter, Collection<String> values) {
        for (String s : values) {
            if (s != null) {
                s = s.trim();
                if (s.length() > 0) {
                    if (value.length() > 0) {
                        value.append(delimiter);
                    }
                    value.append(s);
                }
            }
        }
    }


    public ContactInfo startImport(String name, String email, String notes) {
        ContactInfo contactInfo = new ContactInfo(name, email, notes);
        contacts.add(contactInfo);
        return contactInfo;
    }

    public class ContactInfo {
        String name, email, notes;
        Map<String, Map<String, StringBuilder>> sections;

        ContactInfo(String name, String email, String notes) {
            this.name = name;
            this.email = email;
            this.notes = notes;
            sections = new HashMap<String, Map<String, StringBuilder>>();
        }

        public Map<String, StringBuilder> getSection(String sectionName) {
            sectionNames.add(sectionName);
            Map<String, StringBuilder> section = sections.get(sectionName);
            if (section == null) {
                section = new HashMap<String, StringBuilder>();
                sections.put(sectionName, section);
            }
            return section;
        }

        public void addField(String sectionName, String fieldName, String value) {
            if (value == null || value.length() == 0) return;
            Map<String, StringBuilder> section = getSection(sectionName);
            StringBuilder field = section.get(fieldName);
            if (field == null) {
                field = new StringBuilder(value);
                section.put(fieldName, field);
                return;
            }
            if (field.length() > 0) {
                field.append("; ");
            }
            field.append(value);
        }
    }
}
