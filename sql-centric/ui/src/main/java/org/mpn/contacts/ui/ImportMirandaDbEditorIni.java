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

/**
 * todo [!] Create javadocs for org.mpn.contacts.ui.ImportMirandaDbEditorIni here
 *
 * @author <a href="mailto:pmoukhataev@jnetx.ru">Pavel Moukhataev</a>
 * @version $Revision$
 */
public class ImportMirandaDbEditorIni extends Importer {

    static final Logger log = Logger.getLogger(ImportMirandaDbEditorIni.class);

    static final class PropertiesGroup {
        private Map<String, String> stringProperties = new HashMap<String, String>();
        private Map<String, Integer> integerProperties = new HashMap<String, Integer>();

        public void addString(String name, String value) {
            stringProperties.put(name, value);
        }

        public void addInteger(String name, int value) {
            integerProperties.put(name, value);
        }

        public String getString(String name) {
            return stringProperties.get(name);
        }

        public Integer getInteger(String name) {
            return integerProperties.get(name);
        }
    }

    private static final byte[] buffer = new byte[100000];

    private static final Charset UTF_8_CHARSET = Charset.forName("UTF-8");
    private static final Charset DEFAULT_CHARSET = Charset.forName("Cp1251");

    private static final byte[] CONTACT_SIGNATURE = "CONTACT: ".getBytes();
    private static final byte[] SETTINGS_SIGNATURE = "SETTINGS:".getBytes();

    boolean eof = false;

    String contactName;
    Map<String, PropertiesGroup> contactProperties = new HashMap<String, PropertiesGroup>();
    PropertiesGroup contactSectionProperties;

    public ImportMirandaDbEditorIni() {
        super("Miranda ini", false);
    }

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
                contactSectionProperties = new PropertiesGroup();
                contactProperties.put(sectionName, contactSectionProperties);
            } else if (startsWith(lineBytes, CONTACT_SIGNATURE)) {
                if (contactName != null) {
                    parseContact();
                }
                contactProperties.clear();

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
        parseContact();
    }

    private void parseContact() {
//        log.debug("Contact : " + contactName + ", properties :" + contactProperties);

        contactName = contactProperties.get("CList").getString("MyHandle");
        String groupName = contactProperties.get("CList").getString("Group");

        PropertiesGroup contactPhoto = contactProperties.get("ContactPhoto");
        if (contactPhoto != null) {
            String photoPath = contactPhoto.getString("File");
            if (photoPath != null) {

            }
        }

        String protocol = contactProperties.get("Protocol").getString("p");
        PropertiesGroup protocolProps = contactProperties.get(protocol);
        String id;
        if ("ICQ".equals(protocol)) {
            id = String.valueOf(protocolProps.getInteger("UIN"));
        } else if ("JABBER".equals(protocol)) {
            id = protocolProps.getString("jid");
            Integer isTransport = protocolProps.getInteger("IsTransport");
            if (isTransport.equals(1)) {
                // This is JABBER transport - don't import
                return;
            }
            boolean isTransported = Integer.valueOf(1).equals(protocolProps.getInteger("IsTransported"));
            if (isTransported) {
                String transport = protocolProps.getString("Transport");
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
            id = protocolProps.getString("Username");
        } else {
            log.debug("Unknown protocol : " + protocol);
            return;
        }


        String phone = protocolProps.getString("Phone");
        String phoneCellCar = protocolProps.getString("Cellucar");
        String about = protocolProps.getString("About");
        String fullName = protocolProps.getString("FullName");
        String firstName = protocolProps.getString("FirstName");
        String lastName = protocolProps.getString("LastName");
        String nick = protocolProps.getString("Nick");
        String city = protocolProps.getString("City");
        String country = protocolProps.getString("Country");
        Integer age = protocolProps.getInteger("Age");
        Integer birthYear = protocolProps.getInteger("BirthYear");
        Integer birthMonth = protocolProps.getInteger("BirthMonth");
        Integer birthDay = protocolProps.getInteger("BirthDay");
        String company = protocolProps.getString("Company");
        String companyCountry = protocolProps.getString("CompanyCountry");
        String companyPosition = protocolProps.getString("CompanyPosition");
        String companyOccupation = protocolProps.getString("CompanyOccupation");
        String companyPhone = protocolProps.getString("CompanyPhone");
        String companyCity = protocolProps.getString("CompanyCity");
        String homepage = protocolProps.getString("Homepage");
        Integer gender = protocolProps.getInteger("Gender");

        
        int languageIndex = 1;
        String lastLanguage;
        while (true) {
            lastLanguage = protocolProps.getString("Language" + languageIndex++);
            if (lastLanguage != null) {
                addComment("Language", lastLanguage);
            } else {
                break;
            }
        }


        String interestCategory, interestText;
        int interestIndex = 0;
        while (true) {
            interestCategory = protocolProps.getString("Interest" + interestIndex + "Cat");
            interestText = protocolProps.getString("Interest" + interestIndex + "Text");
            if (interestCategory != null || interestText != null) {
                addComment("Interest", interestCategory + " -> " + interestText);
            } else {
                break;
            }
            interestIndex++;
        }

        /*
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
        */

        // Do import
        addMessaging(id, protocol.toLowerCase());

        setPhonesHome(phone);
        setPhoneMobile(phoneCellCar);
        setAbout(about);
        setFullName(fullName);
        setFirstName(firstName);
        setLastName(lastName);
        setNick(nick);
        setCity(city);
        setCountry(country);
        setAge(age);
        setBirthDay(birthYear, birthMonth, birthDay);
        setCompany(company);
        setCompanyCountry(companyCountry);
        setCompanyCity(companyCity);
        setCompanyPersonPosition(companyPosition);
        setCompanyOccupation(companyOccupation);
        setCompanyPhone(companyPhone);
        setHomepage(homepage);
        if (gender != null) {
            setGender((gender & 1) == 1);
        }

//        addComment("Group");
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
                        String value = new String(lineBytes, i + 2, length, paramType == 'u' ? UTF_8_CHARSET : determineLineCharset(lineBytes, i+2, length));
//                      log.debug(paramName + "=" + value);
                        contactSectionProperties.addString(paramName, value);
                        break;

                    case 'b':
                    case 'w':
                    case 'd':
                        String valueStr = new String(lineBytes, i + 2, length);
                        contactSectionProperties.addInteger(paramName, Integer.parseInt(valueStr));
                        break;

                    default:
                        log.error("Unknown param type " + paramType);
                }

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
        new ImportMirandaDbEditorIni().doImport(new File("C:\\Personal\\Contacts\\2007_11_21\\miranda_contacts-err-dec.ini"));
    }
}
