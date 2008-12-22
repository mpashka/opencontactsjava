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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * todo [!] Create javadocs for org.mpn.contacts.ui.ImportMirandaDbEditorIni here
 *
 * @author <a href="mailto:pmoukhataev@jnetx.ru">Pavel Moukhataev</a>
 * @version $Revision$
 */
public class ImportMirandaDbEditorIni extends ImportMiranda {

    static final Logger log = Logger.getLogger(ImportMirandaDbEditorIni.class);

    private static final byte[] buffer = new byte[100000];

    private static final byte[] CONTACT_SIGNATURE = "CONTACT: ".getBytes();
    private static final byte[] SETTINGS_SIGNATURE = "SETTINGS:".getBytes();

    boolean eof;

    PropertiesGroup contactSectionProperties;

    public void doImport(File mirandaContactsFile) throws IOException {
        eof = false;
        InputStream in = new FileInputStream(mirandaContactsFile);
        Map<String, PropertiesGroup> contactProperties = new HashMap<String, PropertiesGroup>();
        String contactName = null;
        while (!eof) {
            byte[] lineBytes = readLine(in);
            if (lineBytes.length == 0) {
//                log.debug("Section end");
                contactSectionProperties = null;
            } else if (lineBytes[0] == '[') {
                String sectionName = new String(lineBytes, 1, lineBytes.length - 2);
//                log.debug("Section :" + sectionName);
                contactSectionProperties = new PropertiesGroup();
                contactProperties.put(sectionName, contactSectionProperties);
            } else if (startsWith(lineBytes, CONTACT_SIGNATURE)) {
                if (contactName != null) {
                    parseContact(contactProperties);
                }
                contactProperties.clear();
                contactName = null;

                for (int i = lineBytes.length - 1; i >= CONTACT_SIGNATURE.length; i--) {
                    if (lineBytes[i] == ' ') {
                        int spacePos = i;

                        contactName = new String(lineBytes, CONTACT_SIGNATURE.length, spacePos - CONTACT_SIGNATURE.length,
                                determineLineCharset(lineBytes, CONTACT_SIGNATURE.length, spacePos - CONTACT_SIGNATURE.length));
//                        String contactType = new String(lineBytes, spacePos, lineBytes.length - spacePos);

//                        log.debug("Contact : " + contactName);


                        break;
                    }
                }
            } else if (startsWith(lineBytes, SETTINGS_SIGNATURE)) {
                // Settings section - ignore
            } else {
                readLineString(lineBytes);
            }
        }
        parseContact(contactProperties);
    }

    private boolean startsWith(byte[] string, byte[] start) {
        if (string.length < start.length) return false;
        for (int i = 0; i < start.length; i++) {
            if (string[i] != start[i]) return false;
        }
        return true;
    }

    private void readLineString(byte[] lineBytes) {
        for (int i = 0; i < lineBytes.length; i++) {
            byte lineByte = lineBytes[i];
            if (lineByte == '=') {
                String paramName = new String(lineBytes, 0, i);
                char paramType = (char) lineBytes[i+1];
                int length = lineBytes.length - i - 2;
                if (length == 0) {
//                    log.debug("Empty value : " + paramName);
                }
                switch (paramType) {
                    case 'u':
                    case 's':
                        String value = new String(lineBytes, i + 2, length, paramType == 'u' ? UTF8_CHARSET : determineLineCharset(lineBytes, i+2, length));
//                      log.debug(paramName + "=" + value);
                        contactSectionProperties.addString(paramName, value);
                        break;

                    case 'b':
                    case 'w':
                    case 'd':
                        String valueStr = new String(lineBytes, i + 2, length);
                        contactSectionProperties.addInteger(paramName, Integer.parseInt(valueStr));
                        break;

                    case 'B':       // Ignore
                        log.debug("Ignore bytes data : " + paramName);
                        break;

                    default:
                        log.error("Unknown param type " + paramType + " for line : " + new String(lineBytes));
                }
                break;
            }
        }
    }

    private byte[] readLine(InputStream in) throws IOException {
        int pos = 0;
        int nextSymbol;
        eof = true;
        while ((nextSymbol = in.read()) != -1) {
            if (nextSymbol == '\r') {
//                continue;
            } else if (nextSymbol == '\n') {
                eof = false;
                break;
            } else {
                buffer[pos++] = (byte) nextSymbol;
            }
        }
        byte[] line = new byte[pos];
        System.arraycopy(buffer, 0, line, 0, pos);
        return line;
    }

    public static void main(String[] args) throws IOException {
//        log.debug("Default charset : " + DEFAULT_CHARSET);
        new ImportMirandaDbEditorIni().doImport(new File("C:\\Personal\\Contacts\\2007_11_21\\miranda_contacts-err-dec.ini"));
    }
}
