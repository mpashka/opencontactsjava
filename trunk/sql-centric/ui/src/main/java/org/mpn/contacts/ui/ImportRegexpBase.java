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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Map;

/**
 * todo [!] Create javadocs for org.mpn.contacts.ui.ImportRegexpBase here
 *
 * @author <a href="mailto:pmoukhataev@jnetx.ru">Pavel Moukhataev</a>
 * @version $Revision$
 */
public class ImportRegexpBase extends Importer {

    static final Logger log = Logger.getLogger(ImportRegexpBase.class);

    public ImportRegexpBase(String importerName, boolean importCompany) {
        super(importerName, importCompany);
    }

    public void doStreamImport(Pattern delimiter, Map<String, String> predefinedFields, File fileName, Pattern regexp, String[] fieldNames) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)/*, DEFAULT_CHARSET*/));
        char[] buffer = new char[10000];
        StringBuilder data = new StringBuilder();
        int length;
        while ((length = in.read(buffer)) != -1) {
            data.append(buffer, 0, length);
        }
        in.close();
        String[] contacts = delimiter.split(data);
        for (String s : contacts) {
            importContact(regexp, s, fieldNames, predefinedFields);
        }
    }

    public void doImport(Map<String, String> predefinedFields, File fileName, Pattern regexp, String[] fieldNames) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), DEFAULT_CHARSET));
        String inLine;
        while ((inLine = in.readLine()) != null) {
            importContact(regexp, inLine, fieldNames, predefinedFields);
        }
        in.close();
    }

    private void importContact(Pattern regexp, String inLine, String[] fieldNames, Map<String, String> predefinedFields) {
        Matcher matcher = regexp.matcher(inLine);
        if (matcher.matches()) {
            if (matcher.groupCount() != fieldNames.length) {
                log.warn("Group count doesn't match. Line : " + inLine + ", fields : " + fieldNames.length + ", group count : " + matcher.groupCount());
            }
            if (predefinedFields != null) {
                for (Map.Entry<String, String> entry : predefinedFields.entrySet()) {
                    setField(entry.getKey(), entry.getValue());
                }
            }
            for (int i = 0; i < fieldNames.length; i++) {
                String fieldName = fieldNames[i];
                if (fieldName != null) {
                    setField(fieldName, matcher.group(i));
                }
            }
            importContact();
        } else {
            log.debug("Skip line : " + inLine);
        }
    }
}
