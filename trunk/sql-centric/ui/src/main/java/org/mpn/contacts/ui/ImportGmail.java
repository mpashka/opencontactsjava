/*
 * $URL$
 *
 * $Revision$
 *
 * $Date$
 *
 * Copyright notice
 */
package org.mpn.contacts.ui;

import org.apache.log4j.Logger;
import org.mpn.contacts.framework.db.DbAccess;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * todo [!] Create javadocs for org.mpn.contacts.ui.ImportGmail here
 *
 * @author $Author $
 * @version $Revision$
 */
public class ImportGmail extends Importer {

    static final Logger log = Logger.getLogger(ImportGmail.class);

    private static final Charset UTF8_CHARSET = Charset.forName("UTF8");
    private static final Charset UNICODE_CHARSET = Charset.forName("UTF-16");

    private static final String SECTION_HEADER_NAME = "Раздел";
    private static final String SECTION_HEADER_VALUE = "Описание";

    private static final String FIELD_NAME_MOBILE = "Мобильный";
    private static final String FILED_NAME_EMAIL = "Электронная почта";
    private static final String FIELD_NAME_PHONE = "Телефон";

    private static final String[] INTERNET_DOMAINS = {
            "com", "ru", "org", "edu", "gov", "jp", "info", "biz", "tv",
    };

    static {
        Arrays.sort(INTERNET_DOMAINS);
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

    public ImportGmail() {
        super("gmail", false);
    }

    public void doImport(File file) throws Exception {
        BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), UNICODE_CHARSET));
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
                    if (token.length() > 0) {
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
                String fieldValue = fieldValues[i];
                if (checkIsEmail(fieldValue)) {
                    if (!fieldName.equals(FILED_NAME_EMAIL)) {
                        log.debug("Name : " + name + " <" + email + ">");
                        log.debug("E-mail field " + fieldName + ".");
                    }
                } else {
                    if (fieldName.equals(FIELD_NAME_MOBILE)) {
                        setPhoneMobile(fieldValue);
                    } else if (fieldName.equals(FILED_NAME_EMAIL)) {
                        log.error("E-mail is not valid : " + fieldValue);
                    } else if (fieldName.equals(FIELD_NAME_PHONE)) {
                        setPhonesHome(fieldValue);
                    } else {
                        addComment(gmailContactSection.name + "." + fieldName, fieldValue);
                    }
                }
            }
        }


        importContact();
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
        new ImportGmail().doImport(new File("C:\\gmail.csv"));

        Thread.sleep(4000);

        DbAccess.getInstance().close();

    }
}
