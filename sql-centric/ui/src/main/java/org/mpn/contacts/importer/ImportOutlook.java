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
package org.mpn.contacts.importer;

import org.apache.log4j.Logger;
import org.mpn.contacts.ui.Data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Arrays;
import java.util.HashSet;

/**
 * todo [!] Create javadocs for org.mpn.contacts.ui.ImportOutlook here
 *
 * @author <a href="mailto:pmoukhataev@jnetx.ru">Pavel Moukhataev</a>
 * @version $Revision$
 */
public class ImportOutlook extends ImportCsv {

    static final Logger log = Logger.getLogger(ImportOutlook.class);

    private static final String[] STANDARD_HEADERS = {
            "Name","E-mail Address","Notes","E-mail 2","E-mail 3","Mobile Phone","Pager","Company","Job Title","Home Phone","Home Phone 2","Home Fax","Home Address","Business Phone","Business Phone 2","Business Fax","Business Address","Other Phone","Other Fax","Other Address"
    };

    private static final Set<String> NAME_TITLES = new HashSet<String>(Arrays.asList(
            "Title", "Dr.", "Miss", "Mr.", "Mrs.", "Ms.", "Prof."
    ));

    public ImportOutlook() {
        super("Outlook", false);
    }

    private void doImport(File file) throws IOException {
        BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), DEFAULT_CHARSET));

        String headerLine = fileReader.readLine();
        String[] headers = parseLine(headerLine);
        if (!Arrays.equals(headers,  STANDARD_HEADERS)) {
            log.warn("Non-standard outlook headers provided : " + headerLine);
        }

        while (!eof) {
            String[] contactInfoStrings = parseLine(fileReader);
            if (contactInfoStrings.length != headers.length) {
                log.error("Line length is not equal : " + contactInfoStrings.length + ", " + headers.length);
            }
            Map<String, String> fields = new HashMap<String, String>();
            for (int i = 0; i < contactInfoStrings.length; i++) {
                String contactInfoString = contactInfoStrings[i];
                if (contactInfoString != null && contactInfoString.length() > 0) {
                    fields.put(headers[i], contactInfoString);
                }
            }

            importOutlookContact(fields);
        }
        fileReader.close();

    }

    private void importOutlookContact(Map<String,String> fields) {
        String fullName = fields.get("Name");
        setFullName(fullName);
        int commaIndex = fullName.indexOf(',');
        if (commaIndex > 0) {
            String lastName = fullName.substring(0, commaIndex);
            setLastName(lastName);

            String fullFirstName = fullName.substring(commaIndex + 1);
            String[] firstNames = fullFirstName.split(" ", 4);
            String title = null;
            String firstName;
            String middleName = null;
            String suffix = null;
            if (firstNames.length == 4) {
                title = firstNames[0];
                firstName = firstNames[1];
                middleName = firstNames[2];
                suffix = firstNames[3];
                if (!NAME_TITLES.contains(title)) {
                    log.warn("Full name is 4 length, but first is not title : " + fullName);
                }
            } else if (NAME_TITLES.contains(firstNames)) {
                title = firstNames[0];
                firstName = firstNames[1];
                if (firstNames.length > 1) {
                    middleName = firstNames[2];
                }
                if (firstNames.length > 2) {
                    suffix = firstNames[3];
                }
            } else {
                firstName = firstNames[0];
                if (firstNames.length > 0) {
                    middleName = firstNames[1];
                }
                if (firstNames.length > 1) {
                    suffix = firstNames[2];
                }
                if (firstNames.length > 2) {
                    log.warn("Name length is more than 2 but first is not title : " + fullName);
                }
            }

            setFirstName(firstName);
            setMiddleName(middleName);
            addComment("Title", title);
            addComment("Suffix", suffix);
        }

        addMessaging(fields.get("E-mail Address"), Data.IM_TYPE_EMAIL);
        addMessaging(fields.get("E-mail 2"), Data.IM_TYPE_EMAIL);
        addMessaging(fields.get("E-mail 3"), Data.IM_TYPE_EMAIL);
        addPhoneMobile(fields.get("Mobile Phone"));
        setAbout(fields.get("Notes"));
        addComment("Pager", fields.get("Pager"));

        setCompany(fields.get("Company"));
        setCompanyPersonPosition(fields.get("Job Title"));
        addPhoneHome(fields.get("Home Phone"));
        addPhoneHome(fields.get("Home Phone 2"));
        addPhoneHome(fields.get("Home Fax"));
        setAddress(fields.get("Home Address"));
        setCompanyPhone(fields.get("Business Phone"));
        addComment("companyPhone", fields.get("Business Phone 2"));
        addComment("companyFax", fields.get("Business Fax"));
        setCompanyLocation(fields.get("Business Address"));
        addComment("OtherPhone", fields.get("Other Phone"));
        addComment("OtherFax", fields.get("Other Fax"));
        addComment("OtherAddress", fields.get("Other Address"));
        importContact();
    }

    public static void main(String[] args) throws IOException {
        new ImportOutlook().doImport(new File("C:\\Projects\\jContacts\\.data\\test\\outlookImport\\gmail-to-outlook-two.csv"));
    }

}
