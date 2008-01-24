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

import com.sun.xml.internal.messaging.saaj.packaging.mime.internet.MimeUtility;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * todo [!] Create javadocs for org.mpn.contacts.ui.ImportVCard here
 *
 * @author <a href="mailto:pmoukhataev@jnetx.ru">Pavel Moukhataev</a>
 * @version $Revision$
 */
public class ImportVCard extends Importer {

    static final Logger log = Logger.getLogger(ImportVCard.class);

    public ImportVCard() {
        super("Vcard", false);
    }

    public void doImportVCards(File vcardDir) throws IOException {
        if (!vcardDir.isDirectory()) {
            log.error("VCard dir is not directory : " + vcardDir);
            return;
        }

        File[] vcardFiles = vcardDir.listFiles();
        if (vcardFiles == null) {
            log.error("VCard dir has no files : " + vcardDir);
            return;
        }
        for (File file : vcardFiles) {
            doImportVCard(file);
        }
    }

    private String name;
    private String phoneHome;
    private String phoneCell;
    private String phoneFax;

    private char[] buffer = new char[10000];

    public void doImportVCard(File file) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), DEFAULT_CHARSET));

        String inLine;
        while ((inLine = in.readLine()) != null) {
            int commaPos = inLine.indexOf(':');
            if (commaPos == -1) {
                log.warn("Comma not found");
                continue;
            }
            Map<String, String> namesMap = new HashMap<String, String>();
            Set<String> namesSet = new HashSet<String>();
            String namesStr = inLine.substring(0, commaPos);
            String value = inLine.substring(commaPos + 1);
            String[] namesArr = namesStr.split(";");
            for (String s : namesArr) {
                int eqPos = s.indexOf('=');
                if (eqPos == -1) {
                    namesSet.add(s);
                    continue;
                }
                String propertyName = s.substring(0, eqPos);
                String propertyValue = s.substring(eqPos + 1);
                namesMap.put(propertyName, propertyValue);
            }
            if (namesStr.equals("BEGIN")) {
                if (!value.equals("VCARD")) {
                    log.error("Unknown begin : " + value);
                }
                startImportContact();
            } else if (namesStr.equals("END")) {
                if (!value.equals("VCARD")) {
                    log.error("Unknown end : " + value);
                }
                importData();
            } else if (namesSet.contains("N")) {
                String charsetName = namesMap.get("CHARSET");
                String encodingName = namesMap.get("ENCODING");
                // todo [!] use J2EE class here
                byte[] chars = value.getBytes();
                InputStream valueIn = new ByteArrayInputStream(chars);
                try {
                    InputStream encodedIn = MimeUtility.decode(valueIn, encodingName);
                    Reader valueReader = new InputStreamReader(encodedIn, charsetName);
                    int length = valueReader.read(buffer);
                    name = new String(buffer, 0, length);
                } catch (Exception e) {
                    log.error("Error reading value from " + inLine, e);
                }
                valueIn.close();
            } else if (namesSet.contains("TEL")) {
// CELL;{HOME|WORK};{VOICE|FAX};
                if (namesSet.contains("CELL")) {
                    phoneCell = value;
                } else if (namesSet.contains("FAX")) {
                    phoneFax = value;
                } else {
                    phoneHome = value;
                }
            }
        }

    }

    private void importData() {
        setFullName(name);
        addPhoneHome(phoneHome);
        addPhoneMobile(phoneCell);
        addComment("Fax", phoneFax);

        name = phoneHome = phoneCell = phoneFax = null;
    }
}
