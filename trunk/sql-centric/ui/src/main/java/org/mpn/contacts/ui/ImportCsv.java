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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * todo [!] Create javadocs for org.mpn.contacts.ui.ImportCsv here
 *
 * @author <a href="mailto:pmoukhataev@jnetx.ru">Pavel Moukhataev</a>
 * @version $Revision$
 */
public class ImportCsv extends Importer {

    protected boolean eof;


    public ImportCsv(String importerName, boolean importCompany) {
        super(importerName, importCompany);
    }

    public void doImport(File file, Map<String, String> fieldsMapping) throws IOException {
        BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), DEFAULT_CHARSET));

        String headerLine = fileReader.readLine();
        String[] headers = parseLine(headerLine);

        while (!eof) {
            String[] contactInfoStrings = parseLine(fileReader);
            for (int i = 0; i < contactInfoStrings.length; i++) {
                String contactInfoString = contactInfoStrings[i];
                if (contactInfoString != null && contactInfoString.length() > 0) {
                    setField(headers[i], contactInfoString);
                }
            }
            importContact();
        }
        fileReader.close();
    }

    protected String[] parseLine(String line) {
        String[] headerLines = line.split(",");
        for (int i = 0; i < headerLines.length; i++) {
            String headerLine = headerLines[i];
            if (headerLine != null && headerLine.length() >=2 && headerLine.charAt(0) == '"'
                    && headerLine.charAt(headerLine.length() - 1) == '"') {
                headerLine = headerLine.substring(1, headerLine.length() - 2);
                headerLines[i] = headerLine;
            }
        }
        return headerLines;
    }

    protected String[] parseLine(BufferedReader fileReader) throws IOException {
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




}
