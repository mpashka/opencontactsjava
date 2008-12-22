/*
 * $URL$
 *
 * $Revision$
 *
 * $Date$
 *
 * Copyright notice
 */
package org.mpn.contacts.importer;

import org.apache.log4j.Logger;
import org.mpn.contacts.framework.db.DbAccess;
import org.mpn.contacts.ui.Data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * todo [!] Create javadocs for org.mpn.contacts.ui.ImportGmail here
 *
 * @author $Author $
 * @version $Revision$
 */
public class ImportGmail extends ImportCsv {

    static final Logger log = Logger.getLogger(ImportGmail.class);

    private static final String GMAIL_PROPERTIES_FILE_NAME = "gmail.properties";
    private static final String GMAIL_PROPERTIES_DELIMITER = "language";

    private static final String GMAIL_PROPERTY_NAME = "name";

    private final class GmailContactSectionMetainfo {
        String[] fieldNames;

        public GmailContactSectionMetainfo(List<String> sectionNames) {
            this.fieldNames = sectionNames.toArray(new String[sectionNames.size()]);
            sectionNames.clear();
        }

        public void createSection(String[] fileds) {
            fieldNumber++;
        }
    }

    private static final class GmailContactSection {
        GmailContactSectionMetainfo metainfo;
        String name;
        String[] fieldValues;

        public GmailContactSection(GmailContactSectionMetainfo metainfo, String name, String[] fieldValues) {
            this.metainfo = metainfo;
            this.name = name;
            this.fieldValues = fieldValues;
        }
    }

    private int fieldNumber;
    private Map<String, Properties> propertyNames = new HashMap<String, Properties>();

    private String SECTION_HEADER_NAME = "Раздел";
    private String SECTION_HEADER_VALUE = "Описание";

    private String FIELD_NAME_MOBILE = "Мобильный";
    private String FILED_NAME_EMAIL = "Электронная почта";
    private String FIELD_NAME_PHONE = "Телефон";


    public ImportGmail() {
        super("gmail", false);
        try {
            readHeaderFiles();
        } catch (IOException e) {
            log.error("Error reading gmail properties", e);
        }
    }

    public void doImportFolder(File file) {
        log.info("Import gmail folder : " + file);
        for (File file1 : file.listFiles()) {
            if (file1.isFile() && file1.getName().toLowerCase().endsWith(".csv")) {
                try {
                    doImport(file1);
                } catch (Exception e) {
                    log.error("Error importing file : " + file1, e);
                }
            } else if (file1.isDirectory()) {
                doImportFolder(file1);
            }
        }
    }

    public void doImport(File file) throws Exception {
        log.info("Import gmail file : " + file);
        String encoding = EncodingUtils.checkFileEncoding(file);
        BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));
        String headerLine = fileReader.readLine();
        String[] headers = parseLine(headerLine);
        readHeaderNames(headers);

        List<GmailContactSectionMetainfo> contactSectionMetainfos = new ArrayList<GmailContactSectionMetainfo>();
        List<String> sectionNames = new ArrayList<String>();
        for (int i = 3; i < headers.length; i++) {
            String header = headers[i];
            if (header.startsWith(SECTION_HEADER_NAME)) {
                String headerNumberString = header.substring(SECTION_HEADER_NAME.length()).trim();
                int spacePos = headerNumberString.indexOf(' ');
                int headerNumber = Integer.parseInt(headerNumberString.substring(0, spacePos));
                if (headerNumber - 2 == contactSectionMetainfos.size()) {
                    contactSectionMetainfos.add(new GmailContactSectionMetainfo(sectionNames));
                } else if (headerNumber - 1 > contactSectionMetainfos.size()) {
                    log.error("Invalid header number : " + headerNumber + " > " +  contactSectionMetainfos.size());
                }
                String headerName = headerNumberString.substring(spacePos + 3);
                sectionNames.add(headerName);
                if (sectionNames.size() == 0 && !SECTION_HEADER_VALUE.equals(headerName)) {
                    log.error("First section is not name:" + headerName);
                }
            } else {
                log.error("Unknown header : " + header);
            }
        }
        contactSectionMetainfos.add(new GmailContactSectionMetainfo(sectionNames));


        while (!eof) {
            String[] contactInfoStrings = parseLine(fileReader);
            if (contactInfoStrings.length == 0 ||
                    (contactInfoStrings.length == 1 && contactInfoStrings[0] == null))
            {
                break;
            }

            fieldNumber = 3;
            List<GmailContactSection> sections = new ArrayList<GmailContactSection>();
            for (GmailContactSectionMetainfo gmailContactSectionMetainfo : contactSectionMetainfos) {
                if (fieldNumber >= contactInfoStrings.length) break;
                String sectionName = contactInfoStrings[fieldNumber];
                String[] sectionFiledValues = new String[gmailContactSectionMetainfo.fieldNames.length];
                boolean nonEmpty = false;
                for (int i = 0; i < gmailContactSectionMetainfo.fieldNames.length; i++) {
                    if (fieldNumber >= contactInfoStrings.length) break;
//                    String fieldMetName = gmailContactSectionMetainfo.fieldNames[i];
                    String fieldValue = contactInfoStrings[fieldNumber++];
                    sectionFiledValues[i] = fieldValue;
                    if (fieldValue != null) {
                        nonEmpty = true;
//                        checkIsEmail(fieldValue);
                    }
                }
                if (nonEmpty) {
                    GmailContactSection section = new GmailContactSection(gmailContactSectionMetainfo, sectionName, sectionFiledValues);
                    sections.add(section);
                }
            }
            String notes = contactInfoStrings.length > 2 ? contactInfoStrings[2] : null;
            importGmailContact(contactInfoStrings[0], contactInfoStrings[1], notes, sections);
        }
        fileReader.close();
    }

    private void readHeaderNames(String[] headers) {
        Properties gmailHeaderNames = propertyNames.get(headers[0]);
        if (gmailHeaderNames == null) {
            log.error("Error recognizing gmail language : " + headers[0]);
            return;
        }

        SECTION_HEADER_NAME = gmailHeaderNames.getProperty("section");
        SECTION_HEADER_VALUE = gmailHeaderNames.getProperty("description");

        FIELD_NAME_MOBILE = gmailHeaderNames.getProperty("mobile");
        FILED_NAME_EMAIL = gmailHeaderNames.getProperty("email");
        FIELD_NAME_PHONE = gmailHeaderNames.getProperty("phone");

    }

    private void readHeaderFiles() throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(GMAIL_PROPERTIES_FILE_NAME), UTF8_CHARSET));
        Properties properties = new Properties();
        String inLine;
        while ((inLine = in.readLine()) != null) {
            if (inLine.length() == 0) continue;
            else if (inLine.charAt(0) == '#') continue;
            int eqPos = inLine.indexOf('=');
            if (eqPos == -1) {
                log.error("Error reading gmail properties. No eq in line : " + inLine);
                continue;
            }
            String name = inLine.substring(0, eqPos).trim().toLowerCase();
            String value = inLine.substring(eqPos + 1).trim();
            if (name.equalsIgnoreCase(GMAIL_PROPERTIES_DELIMITER)) {
                propertyNames.put(properties.getProperty(GMAIL_PROPERTY_NAME), properties);
                properties = new Properties();
            } else {
                properties.setProperty(name, value);
            }
        }
        if (!properties.isEmpty()) {
            propertyNames.put(properties.getProperty(GMAIL_PROPERTY_NAME), properties);
        }
    }


    private void importGmailContact(String name, String email, String notes, List<GmailContactSection> sections) {
        Pattern pattern = null;

        if (name == null) {
            name = email.replaceFirst("@\\.+$", "");
        }

        if (name.startsWith("ICQ/")) {
            pattern = Pattern.compile("^(ICQ/[/\\.\\w]+) - ");
        } else if (name.startsWith("[jnetx.")) {
            pattern = Pattern.compile("^\\[([.\\p{Lower}]+)\\] ");
        }

        if (pattern != null) {
            Matcher matcher = pattern.matcher(name);
            if (matcher.find()) {
                String groupName = matcher.group(1);
                matcher.appendReplacement(new StringBuffer(), "");
                name = matcher.appendTail(new StringBuffer()).toString();
            } else {
                log.warn("Name starts with ICQ but doesn't match pattern : " + name);
            }
        }

        startImportContact();
        setFullName(name);
        addMessaging(email, Data.IM_TYPE_EMAIL);
        addComment("Notes", notes);

        for (GmailContactSection gmailContactSection : sections) {

            String[] fieldNames = gmailContactSection.metainfo.fieldNames;
            String[] fieldValues = gmailContactSection.fieldValues;
            for (int i = 0; i < fieldNames.length; i++) {
                String fieldName = fieldNames[i];
                String fieldValuesString = fieldValues[i];
                if (fieldValuesString == null || !fieldValuesString.contains(" ::: ")) {
                    importGmailField(name, email, gmailContactSection, fieldName, fieldValuesString);
                } else {
                    String[] fieldValuesArray = fieldValuesString.split(" ::: ");
                    for (String fieldValue : fieldValuesArray) {
                        importGmailField(name, email, gmailContactSection, fieldName, fieldValue);
                    }
                }
            }
        }


        importContact();
    }

    private void importGmailField(String name, String email, GmailContactSection gmailContactSection, String fieldName, String fieldValue) {
        if (checkIsEmail(fieldValue)) {
            if (!fieldName.equals(FILED_NAME_EMAIL)) {
                log.debug("Name : " + name + " <" + email + ">");
                log.debug("E-mail field " + fieldName + ".");
            }
        } else {
            if (fieldName.equals(FIELD_NAME_MOBILE)) {
                addPhoneMobile(fieldValue);
            } else if (fieldName.equals(FILED_NAME_EMAIL)) {
                log.error("E-mail is not valid : " + fieldValue);
            } else if (fieldName.equals(FIELD_NAME_PHONE)) {
                addPhoneHome(fieldValue);
            } else {
                addComment(gmailContactSection.name + "." + fieldName, fieldValue);
            }
        }
    }

    private boolean checkIsEmail(String fieldValue) {
        if (fieldValue == null) return false;
        String lowCaseFieldValue = fieldValue.trim().toLowerCase();
        int dogIndex = lowCaseFieldValue.indexOf('@');
        if (dogIndex > 0 && lowCaseFieldValue.indexOf('@', dogIndex + 1) == -1) {
            for (String s : INTERNET_DOMAINS) {
                if (lowCaseFieldValue.endsWith("." + s)) { // this is e-mail
                    boolean jabber = lowCaseFieldValue.indexOf("jabber") > 0;
                    // check if this is ICQ
                    String emailName = lowCaseFieldValue.substring(0, dogIndex);
                    try {
                        long icqNumber = Long.parseLong(emailName);
                        if (lowCaseFieldValue.indexOf("icq") > 0 || lowCaseFieldValue.indexOf("aim") > 0 /*|| jabber*/) {
                            addMessaging(emailName, Data.IM_TYPE_ICQ);
                            return true;
                        } else {
                            log.info("Field looks like ICQ, but is not : " + lowCaseFieldValue);
                        }
                    } catch (NumberFormatException e) {
                        if (jabber) {
                            addMessaging(lowCaseFieldValue, Data.IM_TYPE_JABBER);
                            return true;
                        } else {
                            addMessaging(lowCaseFieldValue, Data.IM_TYPE_EMAIL);
                            return true;
                        }
                    }
                }
            }
            log.warn("@ present, but is not valid inet domain : " + fieldValue);
        }
        return false;
    }


    public static void main(String[] args) throws Exception {
        new ImportGmail().doImport(new File("../.#data/import/gmail/2006-04-11/gmail-utf.csv"));

        Thread.sleep(4000);

        DbAccess.getInstance().close();

    }
}
