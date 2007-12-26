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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Arrays;

/**
 * todo [!] Create javadocs for org.mpn.contacts.ui.ImportMirandaDbEditor here
 *
 * @author <a href="mailto:pmoukhataev@jnetx.ru">Pavel Moukhataev</a>
 * @version $Revision$
 */
public class ImportMirandaDbEditor {

    static final Logger log = Logger.getLogger(ImportMirandaDbEditor.class);

    private static final byte[] buffer = new byte[100000];

    private static final Charset UTF_8_CHARSET = Charset.forName("UTF-8");
    private static final Charset DEFAULT_CHARSET = Charset.forName("Cp1251");

    private static final byte[] CONTACT_SIGNATURE = "CONTACT: ".getBytes();

    boolean eof = false;

    String contactName;
    Map<String, Map<String, String>> contactProperties = new HashMap<String, Map<String, String>>();
    Map<String, String> contactSectionProperties;

    public void doImport(File mirandaContactsFile) throws IOException {
        InputStream in = new FileInputStream(mirandaContactsFile);
        while (!eof) {
            byte[] lineBytes = readLine(in);
            if (lineBytes.length == 0) {
//                log.debug("Section end");
                contactSectionProperties = null;
            } else if (lineBytes[0] == '[') {
                String sectionName = new String(lineBytes, 1, lineBytes.length - 2);
//                log.debug("Section :" + sectionName);
                contactSectionProperties = new HashMap<String, String>();
                contactProperties.put(sectionName, contactSectionProperties);
            } else if (startsWith(lineBytes, CONTACT_SIGNATURE)) {
                if (contactName != null) {
                    parseContact();
                }

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
            } else {
                readLineString(lineBytes);
            }
        }
        parseContact();
    }

    private void parseContact() {
//        log.debug("Contact : " + contactName + ", properties :" + contactProperties);

        String customName = contactProperties.get("CList").get("MyHandle");
        String groupName = contactProperties.get("CList").get("Group");

        Map<String, String> contactPhoto = contactProperties.get("ContactPhoto");
        if (contactPhoto != null) {
            String photoPath = contactPhoto.get("File");
            if (photoPath != null) {

            }
        }

        String protocol = contactProperties.get("Protocol").get("p");
        Map<String, String> protocolProps = contactProperties.get(protocol);
        String id;
        if ("ICQ".equals(protocol)) {
            id = protocolProps.get("UIN");
        } else if ("JABBER".equals(protocol)) {
            id = protocolProps.get("jid");
            String isTransport = protocolProps.get("IsTransport");
            if (isTransport.equals("1")) {
                // This is JABBER transport - don't import
                return;
            }
            boolean isTransported = "1".equals(protocolProps.get("IsTransported"));
            if (isTransported) {
                String transport = protocolProps.get("Transport");
                int alpPos = id.indexOf('@');
                id = id.substring(0, alpPos);
                if (transport.startsWith("msn.")) {
                    protocol = "MSN";
                    id = id.replace('%', '@');
                } else if (transport.startsWith("icq")) {
                    protocol = "ICQ";
                } else {
                    log.debug("Unknown transport : " + transport);
                }
            }
        } else if ("SKYPE".equals(protocol)) {
            id = protocolProps.get("Username");
        } else {
            log.debug("Unknown protocol : " + protocol);
            return;
        }


        String phone = protocolProps.get("Phone");
        String phoneCellCar = protocolProps.get("Cellucar");
        String about = protocolProps.get("About");
        String fullName = protocolProps.get("FullName");
        String firstName = protocolProps.get("FirstName");
        String lastName = protocolProps.get("LastName");
        String nick = protocolProps.get("Nick");
        String city = protocolProps.get("City");
        String country = protocolProps.get("Country");
        String age = protocolProps.get("Age");
        String birthYear = protocolProps.get("BirthYear");
        String birthMonth = protocolProps.get("BirthMonth");
        String birthDay = protocolProps.get("BirthDay");
        String company = protocolProps.get("Company");
        String companyCountry = protocolProps.get("CompanyCountry");
        String companyPosition = protocolProps.get("CompanyPosition");
        String companyOccupation = protocolProps.get("CompanyOccupation");
        String companyPhone = protocolProps.get("CompanyPhone");
        String companyCity = protocolProps.get("CompanyCity");
        String homepage = protocolProps.get("Homepage");
        String gender = protocolProps.get("Gender");

        
        int languageIndex = 1;
        String lastLanguage;
        while (true) {
            lastLanguage = protocolProps.get("Language" + languageIndex++);
            if (lastLanguage != null) {
                // todo [!] store language
            } else {
                break;
            }
        }


        String interestCategory, interestText;
        int interestIndex = 0;
        while (true) {
            interestCategory = protocolProps.get("Interest" + interestIndex + "Cat");
            interestText = protocolProps.get("Interest" + interestIndex + "Text");
            if (interestCategory != null || interestText != null) {
                // todo [!] store intereset
            } else {
                break;
            }
            interestIndex++;
        }

        List<String> knownProperties = Arrays.asList(
                "Phone",
                "About",
                "Cellucar",
                "FullName",
                "FirstName",
                "LastName",
                "Nick",
                "City",
                "Country",
                "Age",
                "BirthYear",
                "BirthMonth",
                "BirthDay",
                "Company",
                "CompanyCountry",
                "CompanyPosition",
                "CompanyOccupation",
                "CompanyPhone",
                "CompanyCity",
                "Homepage",
                "Gender",
                "Language1","Language2","Language3","Language4","Language5",
                "Interest0Cat", "Interest0Text", "Interest1Cat", "Interest1Text", "Interest2Cat", "Interest2Text", "Interest3Cat", "Interest3Text", "Interest4Cat", "Interest4Text",

                "UIN", "jid", "Username",
                "Status", "State", "SrvPermitId", "SrvDenyId", "Auth", "ServerData", "UnicodeSend", "MirVer",
                "SRMMStatusIconFlags0", "Transport", "IsTransport", "IsTransported",
                "AvatarType", "AvatarSaved", "AvatarHash", "SRMMStatusIconFlags0", "AvatarXVcard"
                );
        protocolProps.keySet().removeAll(knownProperties);

        if (!protocolProps.isEmpty()) {
            log.debug("Unknown properties found : " + protocolProps);
        }

        contactProperties.clear();
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
                String value = new String(lineBytes, i + 2, length, paramType == 'u' ? UTF_8_CHARSET : determineLineCharset(lineBytes, i+2, length));
//                log.debug(paramName + "=" + value);
                contactSectionProperties.put(paramName, value);
            }
        }
    }

    private Charset determineLineCharset(byte[] lineBytes, int offset, int length) {
        int unicodeSymbolCount = 0;
        for (int i = offset; i < length + offset; i++) {
            byte lineByte = lineBytes[i];
            if (lineByte == -48) {
                unicodeSymbolCount++;
            }
        }

        boolean unicode = unicodeSymbolCount > length / 4;
        return unicode ? UTF_8_CHARSET : DEFAULT_CHARSET;
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
        new ImportMirandaDbEditor().doImport(new File("C:\\Personal\\Contacts\\2007_11_21\\miranda_contacts-err-dec.ini"));
    }
}
