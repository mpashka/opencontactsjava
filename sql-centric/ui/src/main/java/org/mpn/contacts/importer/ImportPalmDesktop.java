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

/**
 * todo [!] Create javadocs for org.mpn.contacts.ui.ImportOutlook here
 *
 * @author <a href="mailto:pmoukhataev@jnetx.ru">Pavel Moukhataev</a>
 * @version $Revision$
 */
public class ImportPalmDesktop extends ImportCsv {

    static final Logger log = Logger.getLogger(ImportPalmDesktop.class);

    public ImportPalmDesktop() {
        super("Palm", false);
    }

    public void doImport(File file) throws IOException {
        eof = false;
        BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), DEFAULT_CHARSET));

        while (!eof) {
            String[] contactInfoStrings = parseLine(fileReader);

            try {
                importPalmContact(contactInfoStrings);
            } catch (Exception e) {
                log.error("Exception occured", e);
            }
        }
        fileReader.close();

    }

    private void importPalmContact(String[] fields) {
        startImportContact();

        String lastName = fields[0];
        setLastName(lastName);

        String firstName = fields[1];
        if (firstName != null) {
            String[] firstNames = firstName.split(" ", 2);
            if (firstNames.length > 1) {
                firstName = firstNames[0];
                String middleName = firstNames[1];
                setMiddleName(middleName);
            }
            setFirstName(firstName);
        }

        setCompanyPersonPosition(fields[2]);
        setCompany(fields[3]);
        for (int i = 4; i < 8; i++) {
            addMessaging(fields[i]);
        }
        setAddress(fields[9]);
        setCity(fields[10]);
        addComment("AddressRegion", fields[11]); // Address region
        addComment("AddressIndex", fields[12]); // Address index
        setCountry(fields[13]);
        setAbout(fields[18]);
        setGroup(fields[20]); // group
        setAddress(fields[23]);
        setCity(fields[24]); // one more city
        importContact();
    }

    private void addMessaging(String id) {
        if (id == null) return;
        id = id.trim().toLowerCase();
        if (isEmail(id)) {
            addMessaging(id, Data.IM_TYPE_EMAIL);
        } else {
            addPhoneHome(id);
        }
    }

    public static void main(String[] args) throws IOException {
        new ImportPalmDesktop().doImport(new File("C:\\Projects\\jContacts\\.data\\test\\outlookImport\\gmail-to-outlook-two.csv"));
    }

}
