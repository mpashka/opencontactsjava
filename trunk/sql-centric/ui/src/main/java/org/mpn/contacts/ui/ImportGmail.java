/*
 * Copyright (c) 2005-2007 jNetX.
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
package org.mpn.contacts.ui;

import org.apache.log4j.Logger;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

/**
 * todo [!] Create javadocs for org.mpn.contacts.ui.ImportGmail here
 *
 * @author <a href="mailto:pmoukhataev@jnetx.ru">Pavel Moukhataev</a>
 * @version $Revision$
 */
public class ImportGmail {

    static final Logger log = Logger.getLogger(ImportGmail.class);

    private static final Charset UTF8_CHARSET = Charset.forName("UTF8");

    private static final String SECTION_HEADER_NAME = "Раздел";
    private static final String SECTION_HEADER_VALUE = "Описание";

    private static final String[] INTERNET_DOMAINS = {
            "com", "ru", "org", "edu", "gov", "jp",
    };
    static {
        Arrays.sort(INTERNET_DOMAINS);
    }

    private static final class GmailContactInfo {
        String name;
        String email;
        String notes;
        List<GmailContactSection> sections;
        Collection<String> emails;
        Collection<String> jabbers;
        Collection<Long> icqs;

        public GmailContactInfo(String name, String email, String notes, List<GmailContactSection> sections, Collection<String> emails, Collection<String> jabbers, Collection<Long> icqs) {
            this.name = name;
            this.email = email;
            this.notes = notes;
            this.sections = sections;
            this.emails = emails;
            this.jabbers = jabbers;
            this.icqs = icqs;
        }
    }

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

    private boolean eof;
    private int fieldNumber;
    private HashSet<String> emails;
    private HashSet<String> jabbers;
    private HashSet<Long> icqs;

    public void doImport(File file) throws Exception {
        BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), UTF8_CHARSET));
        String headerLine = fileReader.readLine();
        String[] headers = parseLine(headerLine);
        List<GmailContactSectionMetainfo> contactSectionMetainfos = new ArrayList<GmailContactSectionMetainfo>();
        List<String> sectionNames = new ArrayList<String>();
        for (int i = 3; i < headers.length; i++) {
            String header = headers[i];
            if (header.startsWith(SECTION_HEADER_NAME)) {
                String headerNumberString = header.substring(SECTION_HEADER_NAME.length()).trim();
                int spacePos = headerNumberString.indexOf(' ');
                int headerNumber = Integer.parseInt(headerNumberString.substring(0, spacePos));
                if (headerNumber > contactSectionMetainfos.size()) {
                    log.error("Invalid header number : " + headerNumber + " > " +  contactSectionMetainfos.size());
                } else if (headerNumber == contactSectionMetainfos.size()) {
                    contactSectionMetainfos.add(new GmailContactSectionMetainfo(sectionNames));
                }
                String headerName = headerNumberString.substring(spacePos + 1);
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
            fieldNumber = 3;
            emails = new HashSet<String>();
            jabbers = new HashSet<String>();
            icqs = new HashSet<Long>();
            List<GmailContactSection> sections = new ArrayList<GmailContactSection>();
            for (GmailContactSectionMetainfo gmailContactSectionMetainfo : contactSectionMetainfos) {
                String sectionName = contactInfoStrings[fieldNumber];
                String[] sectionFiledValues = new String[gmailContactSectionMetainfo.fieldNames.length];
                boolean nonEmpty = false;
                for (int i = 0; i < gmailContactSectionMetainfo.fieldNames.length; i++) {
//                    String fieldMetName = gmailContactSectionMetainfo.fieldNames[i];
                    String fieldValue = contactInfoStrings[fieldNumber++];
                    sectionFiledValues[i] = fieldValue;
                    if (fieldValue != null) {
                        nonEmpty = true;
                        checkIsEmail(fieldValue);
                    }
                }
                if (nonEmpty) {
                    GmailContactSection section = new GmailContactSection(gmailContactSectionMetainfo, sectionName, sectionFiledValues);
                    sections.add(section);
                }
            }
            GmailContactInfo contactInfo = new GmailContactInfo(contactInfoStrings[0], contactInfoStrings[1], contactInfoStrings[2],
                    sections, emails, jabbers, icqs);
        }
        fileReader.close();
    }

    private void checkIsEmail(String fieldValue) {
        String lowCaseFieldValue = fieldValue.trim().toLowerCase();
        int dogIndex = lowCaseFieldValue.indexOf('@');
        if (dogIndex > 0 && lowCaseFieldValue.indexOf('@', dogIndex) == -1) {
            for (String s : INTERNET_DOMAINS) {
                if (lowCaseFieldValue.endsWith("." + s)) { // this is e-mail
                    boolean jabber = lowCaseFieldValue.indexOf("jabber") > 0;
                    // check if this is ICQ
                    String emailName = lowCaseFieldValue.substring(0, dogIndex);
                    try {
                        long icqNumber = Long.parseLong(emailName);
                        if (lowCaseFieldValue.indexOf("icq") > 0 || lowCaseFieldValue.indexOf("aim") > 0 /*|| jabber*/) {
                            icqs.add(icqNumber);
                        } else {
                            log.info("Field looks like ICQ, but is not : " + lowCaseFieldValue);
                        }
                    } catch (NumberFormatException e) {
                        if (jabber) {
                            jabbers.add(lowCaseFieldValue);
                        } else {
                            emails.add(lowCaseFieldValue);
                        }
                    }
                }
            }
        }
    }

    private String[] parseLine(String line) {
        return line.split(",");
    }

    private String[] parseLine(BufferedReader fileReader) throws IOException {
        List<String> strings = new ArrayList<String>();
        boolean eol = false;
        do {
            int nextCharInt;
            nextCharInt = fileReader.read();
            if (nextCharInt == '\r') {
                nextCharInt = fileReader.read();
            }

            StringBuilder token = new StringBuilder();
            if (nextCharInt == '"') {
                while (true) {
                    nextCharInt = fileReader.read();
                    if (nextCharInt == '"') {
                        nextCharInt = fileReader.read();
                        if (nextCharInt == ',') {
                            break;
                        } else if (nextCharInt == '\n') {
                            eol = true;
                            break;
                        } else if (nextCharInt == -1) {
                            eol = true;
                            eof = true;
                            break;
                        }
                    }
                    token.append((char) nextCharInt);
                }
            } else {
                while (true) {
                    if (token.length() > 1) {
                        nextCharInt = fileReader.read();
                    }
                    if (nextCharInt == ',') {
                        break;
                    } else if (nextCharInt == '\n') {
                        eol = true;
                        break;
                    } else if (nextCharInt == -1) {
                        eol = true;
                        eof = true;
                        break;
                    }
                    token.append((char) nextCharInt);
                }
            }
            strings.add(token.length() > 0 ? token.toString() : null);
        } while (!eol);
        return strings.toArray(new String[strings.size()]);
    }



    public static void main(String[] args) {

    }
}
