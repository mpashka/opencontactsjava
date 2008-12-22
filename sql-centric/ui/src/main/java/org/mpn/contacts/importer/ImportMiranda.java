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

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * todo [!] Create javadocs for org.mpn.contacts.ui.ImportMirandaDbEditorIni here
 *
 * @author <a href="mailto:pmoukhataev@jnetx.ru">Pavel Moukhataev</a>
 * @version $Revision$
 */
public abstract class ImportMiranda extends Importer {

    static final Logger log = Logger.getLogger(ImportMiranda.class);

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


    public ImportMiranda() {
        super("Miranda", false);
    }

    protected void parseContact(Map<String, PropertiesGroup> contactProperties) {
//        log.debug("Contact : " + contactName + ", properties :" + contactProperties);

        PropertiesGroup contactListProperties = contactProperties.get("CList");
        String contactName = null, groupName = null;
        if (contactListProperties != null) {
            contactName = contactListProperties.getString("MyHandle");
            groupName = contactListProperties.getString("Group");
        } else {
            log.debug("Contact list not defined for contact - hidden contact");
        }

        ImportMiranda.PropertiesGroup contactPhoto = contactProperties.get("ContactPhoto");
        if (contactPhoto != null) {
            String photoPath = contactPhoto.getString("File");
            if (photoPath != null) {

            }
        }

        PropertiesGroup protocolProperties = contactProperties.get("Protocol");
        if (protocolProperties == null) {
            log.debug("Protocol properties not defined");
        }
        String protocol = protocolProperties.getString("p");
        ImportMiranda.PropertiesGroup protocolProps = contactProperties.get(protocol);
        String id;
        if ("ICQ".equals(protocol)) {
            id = String.valueOf(protocolProps.getInteger("UIN"));
        } else if (protocol.startsWith("JABBER")) {     // We check starts with because several
            id = protocolProps.getString("jid");
            Integer isTransport = protocolProps.getInteger("IsTransport");
            if (isTransport != null && isTransport.equals(1)) {
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
                    ImportMiranda.log.debug("Unknown transport : " + transport);
                }
            }
        } else if ("SKYPE".equals(protocol)) {
            id = protocolProps.getString("Username");
        } else {
            ImportMiranda.log.debug("Unknown protocol : " + protocol);
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

        startImportContact();

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

        addPhoneHome(phone);
        addPhoneMobile(phoneCellCar);
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

        addComment("Name", contactName);

        setGroup(groupName);

        importContact();
    }

    protected Charset determineLineCharset(byte[] lineBytes, int offset, int length) {
        int unicodeSymbolCount = 0;
        for (int i = offset; i < length + offset; i++) {
            byte lineByte = lineBytes[i];
            if (lineByte == -48) {
                unicodeSymbolCount++;
            }
        }

        boolean unicode = unicodeSymbolCount > length / 4;
        return unicode ? UTF8_CHARSET : DEFAULT_CHARSET;
    }

}
