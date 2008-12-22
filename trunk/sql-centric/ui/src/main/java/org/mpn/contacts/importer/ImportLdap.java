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
import org.mpn.contacts.framework.db.DbAccess;
import org.mpn.contacts.ui.Data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.sql.SQLException;

/**
 * todo [!] Create javadocs for org.mpn.contacts.ui.ImportLdap here
 *
 * @author <a href="mailto:pmoukhataev@jnetx.ru">Pavel Moukhataev</a>
 * @version $Revision$
 */
public class ImportLdap extends Importer {

    static final Logger log = Logger.getLogger(ImportLdap.class);

    private String companyName;

    public ImportLdap() {
        super("ldap", true);
    }

    public void doImport(String companyName, File ldapFile) throws IOException {
        this.companyName = companyName;
        checkOrganization();

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

        String objectCategory = section.get("objectCategory");
        if (objectCategory == null || !objectCategory.contains("CN=Person")) return;

        startImportContact();

        String icq = section.get("pager");
        addMessaging(icq, Data.IM_TYPE_ICQ);

        String mail = section.get("mail");
        addMessaging(mail, Data.IM_TYPE_EMAIL);
        setCompanyPersonEmail(mail);

        String skype = section.get("ipPhone");
        addMessaging(skype, Data.IM_TYPE_SKYPE);

        setFullName(section.get("name"));
        setFirstName(section.get("givenName"));
        setMiddleName(section.get("initials"));
        setLastName(section.get("sn"));

        String companyPhone = section.get("telephoneNumber");
        setCompanyPhone(companyPhone);

        String companyPosition = section.get("title");
        setCompanyPersonPosition(companyPosition);

        String homePhone = section.get("homePhone");
        addPhoneHome(homePhone);

        String mobilePhone = section.get("mobile");
        addPhoneMobile(mobilePhone);

        String department = section.get("department");
        setCompanyDepartment(department);

        String roomName = section.get("physicalDeliveryOfficeName");
        setCompanyLocation(roomName);

        setCompany(companyName);

        importContact();
        section.clear();
    }

    private void checkOrganization() {
        if (!search(organizationRow, Data.organizationName, companyName)) {
            beginRowUpdate(organizationRow);
            setRowField(Data.organizationName, companyName);
            endRowUpdate(true, organizationRow);
        }
    }

    public static void main(String[] args) throws IOException, SQLException, InterruptedException {

        ImportLdap importLdap = new ImportLdap();

        importLdap.doImport("jnetx", new File("C:\\Personal\\Contacts\\2007_03_26\\jnetx.ldif"));


        Thread.sleep(4000);

        DbAccess.getInstance().close();
    }
}
