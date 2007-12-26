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
import java.util.Map;
import java.util.HashMap;

/**
 * todo [!] Create javadocs for org.mpn.contacts.ui.ImportLdap here
 *
 * @author <a href="mailto:pmoukhataev@jnetx.ru">Pavel Moukhataev</a>
 * @version $Revision$
 */
public class ImportLdap {

    static final Logger log = Logger.getLogger(ImportLdap.class);

    public void doImport(File ldapFile) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(ldapFile));

        String inLine;
        StringBuilder previousLine = new StringBuilder();
        Map<String, String> section = new HashMap<String, String>();
        while ((inLine = in.readLine()) != null) {
            if (inLine.length() == 0) { // End of section
                addSection(section, previousLine);
            } else if (inLine.charAt(0) == ' ') {
                previousLine.append(inLine.substring(1));
            } else {
                addPreviousLine(section, previousLine);
                previousLine.append(inLine);
            }
        }
        addSection(section, previousLine);
        in.close();
    }

    private void addPreviousLine(Map<String, String> section, StringBuilder line) {
        if (line.length() == 0) {
            return;
        }
        int eqPos = line.indexOf(": ");
        if (eqPos < 0) {
            log.error("Unknown ldap line : '" + line + '\'');
            return;
        }
        String name = line.substring(0, eqPos);
        String value = line.substring(eqPos + 2);
        section.put(name, value);
        line.setLength(0);
    }

    private void addSection(Map<String, String> section, StringBuilder lastLine) {
        addPreviousLine(section, lastLine);
//        log.debug("Section : " + section);
    }

    public static void main(String[] args) throws IOException {
        new ImportLdap().doImport(new File("C:\\Personal\\Contacts\\2007_03_26\\jnetx.ldif"));
    }
}
