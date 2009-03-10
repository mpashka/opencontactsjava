/*
 * Copyright (c) 2005-2008 jNetX.
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * todo [!] Create javadocs for org.mpn.contacts.importer.TestProperties here
 *
 * @author <a href="mailto:pmoukhataev@jnetx.ru">Pavel Moukhataev</a>
 * @version $Revision$
 */
public class TestProperties {

    static final Logger log = Logger.getLogger("slee.TestProperties");
    static final Charset UTF8_CHARSET = Charset.forName("UTF-8");

    private static final TestProperties INSTANCE = new TestProperties();

    private Map<String, String> properties;

    private TestProperties() {
        try {
            File propertiesFile = new File("~/jcontacts.properties");
            if (!propertiesFile.exists()) {
                propertiesFile = new File("jcontacts.properties");
                if (!propertiesFile.exists()) {
                    throw new IllegalArgumentException("Properties file not found");
                }
            }

            log.info("Reading properties from file : " + propertiesFile.getCanonicalPath());
            BufferedReader in = new BufferedReader(new FileReader(propertiesFile /*, UTF8_CHARSET*/));
            properties = new HashMap<String, String>();
            String inLine;
            while ((inLine = in.readLine()) != null) {
                if (inLine.length() == 0) continue;
                else if (inLine.charAt(0) == '#') continue;
                int eqPos = inLine.indexOf('=');
                if (eqPos == -1) {
                    log.error("Error reading properties. No eq in line : " + inLine);
                    continue;
                }
                String name = inLine.substring(0, eqPos).trim().toLowerCase();
                String value = inLine.substring(eqPos + 1).trim();
                if (value.endsWith("\\")) {
                    StringBuilder valueMultiline = new StringBuilder();
                    String inLine1 = value;
                    do {
                        if (inLine1.startsWith("#")) continue;
                        inLine1 = inLine1.trim();
                        if (inLine1.endsWith("\\")) {
                            valueMultiline.append(inLine1.subSequence(0, inLine1.length() - 1));
                        } else {
                            valueMultiline.append(inLine1);
                            break;
                        }
                    } while ((inLine1 = in.readLine()) != null);
                    value = valueMultiline.toString();
                }
                properties.put(name, value);
            }
        } catch (IOException e) {
            log.error("Inetrnal Exception occured", e);
        }
    }

    public static TestProperties getInstance() {
        return INSTANCE;
    }

    public String getGoogleLogin() {
        return properties.get("google.login");
    }

    public String getGooglePassword() {
        return properties.get("google.password");
    }

    public String getProperty(String name) {
        String value = properties.get(name);
        if (value == null) {
            throw new IllegalArgumentException("Property not found : " + value);
        }
        return value;
    }
}
