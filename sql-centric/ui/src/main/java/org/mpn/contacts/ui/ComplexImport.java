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
import org.mpn.contacts.importer.ImportCsv;
import org.mpn.contacts.importer.ImportGmail;
import org.mpn.contacts.importer.ImportHtml;
import org.mpn.contacts.importer.ImportLdap;
import org.mpn.contacts.importer.ImportMirandaDb;
import org.mpn.contacts.importer.ImportMirandaDbEditorIni;
import org.mpn.contacts.importer.ImportPalmDesktop;
import org.mpn.contacts.importer.ImportRegexpBase;
import org.mpn.contacts.importer.ImportVCard;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * todo [!] Create javadocs for org.mpn.contacts.ui.ComplexImport here
 *
 * @author <a href="mailto:pmoukhataev@jnetx.ru">Pavel Moukhataev</a>
 * @version $Revision$
 */
public class ComplexImport {

    private static final Logger log = Logger.getLogger(ComplexImport.class);

    public void doImport() throws IOException {
        ImportLdap importLdap = new ImportLdap();
        ImportHtml importHtml = new ImportHtml();

        File rootImportDir = new File("../.#data/import");

        importLdap.doImport("jnetx", new File(rootImportDir, "ldap/jnetx-addrs.2005-10-27.ldif"));
        importLdap.doImport("jnetx", new File(rootImportDir, "ldap/jnetx.adress-book.2007-02-01.ldif"));
        importLdap.doImport("jnetx", new File(rootImportDir, "ldap/jnetx.2007-03-26.ldif"));
        importLdap.doImport("jnetx", new File(rootImportDir, "ldap/jnetx-users.2008-01-23.ldif"));

        importHtml.doImportFolder("jnetx", new File(rootImportDir, "html/2007-12-21"));

        importLdap.doImport("vc", new File(rootImportDir, "ldap/vc.2005-07-07/japan.ldif"));
        importLdap.doImport("vc", new File(rootImportDir, "ldap/vc.2005-07-07/moscow.ldif"));
        importLdap.doImport("vc", new File(rootImportDir, "ldap/vc.2005-07-07/russia-all.ldif"));


        Map<String, String> definedCompanyName = new HashMap<String, String>();
        definedCompanyName.put("company", "artezio");
        ImportRegexpBase importRegexpBase = new ImportRegexpBase("Regex", true);
        importRegexpBase.doImport(definedCompanyName, new File(rootImportDir, "csv/artezio-2002-07-23.txt"),
                Pattern.compile("^([^<]+) *<(([\\p{Lower}]+)@artezio.ru)>[, -]*(.*)$"),
                new String[] {
                        null,
                        "fullName", "messagingEmail", "nick", "about"
                });

        ImportCsv importCsv = new ImportCsv("Csv", false);
        Map<String, String> mobileContactsMapping = new HashMap<String, String>();
        mobileContactsMapping.put("Фамилия", "fullName");
        mobileContactsMapping.put("Телефон", "messagingMobile");
        importCsv.setWarningUnknownFields(false);
        // todo [!] add valuecommerce company here
        importCsv.doImport(new File(rootImportDir, "csv/MobileContacts-ValueCommerce-2005-02-07.txt"), mobileContactsMapping);



        ImportVCard importVCard = new ImportVCard();
        importVCard.doImportVCard(new File(rootImportDir, "csv/Mobile Phone Manager.2005-03-29/vcard.txt"));

        definedCompanyName.put("company", "artezio");
        importRegexpBase.doImport(definedCompanyName, new File(rootImportDir, "csv/artezio-list.2004-01-15.txt"),
                Pattern.compile("^(.+)$"), new String[] {"fullName"});

        definedCompanyName.put("company", "vc");
        importRegexpBase.doImport(definedCompanyName, new File(rootImportDir, "csv/vc-list.2004-01-15.txt"),
                Pattern.compile("^(.+)$"), new String[] {"fullName"});


        importRegexpBase.setImportCompany(false);
        importRegexpBase.doStreamImport(Pattern.compile(",[\\s]*"), null, new File(rootImportDir, "csv/e-mail.contacts.2005-09-21.txt"),
                Pattern.compile("\\\"?([^<]*)[\" ]*<([^>]+)>"), new String[]{"fullName", "messagingEmail"});



        ImportGmail importGmail = new ImportGmail();
        importGmail.doImportFolder(new File(rootImportDir, "gmail"));

        importMirandaFolder(new File(rootImportDir, "Miranda"));


        Map<String, String> batFieldsMapping = new HashMap<String, String>();
        batFieldsMapping.put("First Name", "firstName");
        batFieldsMapping.put("Middle Name", "middleName");
        batFieldsMapping.put("Last Name", "lastName");
        batFieldsMapping.put("Phone", "phonesHome");
        batFieldsMapping.put("E-mail", "messagingEmail");
        batFieldsMapping.put("Categories", "groups");
        batFieldsMapping.put("Mobile", "messagingMobile");
        importCsv.setWarningUnknownFields(false);
        importCsv.doImport(new File(rootImportDir, "csv/2002.06/TheBatContacts.CSV"), batFieldsMapping);

        importRegexpBase.doImport(null, new File(rootImportDir, "csv/e-mail.contacts2.2006-04-11.csv"),
                Pattern.compile("^([^;]+) ;(.+)$"), new String[]{"fullName", "messagingEmail"});

// This is palm desktop contacts
//        importCsv.doImport(new File(rootImportDir, "excel\\contacts.2006-04-14.csv"), new HashMap<String, String>());
//        importCsv.doImport(new File(rootImportDir, "excel\\contacts.memo.2006-04-14.csv"), new HashMap<String, String>());

        ImportPalmDesktop importPalmDesktop = new ImportPalmDesktop();
        importPalmDesktop.doImport(new File(rootImportDir, "PalmDesktop/СтарыеКонтактыPalmDesktop.2006-04-11.csv"));
        importPalmDesktop.doImport(new File(rootImportDir, "PalmDesktop/2006-04-13/contacts-comma.csv"));
        importCsv.doImport(new File(rootImportDir, "PalmDesktop/2006-04-13/excel/contacts.memo.2006-04-14.csv"), new HashMap<String, String>());

        importVCard.doImportVCards(new File(rootImportDir, "vcard"));
    }

    private ImportMirandaDb importMirandaDb = new ImportMirandaDb();
    private ImportMirandaDbEditorIni importMirandaDbEditorIni = new ImportMirandaDbEditorIni();
    private void importMirandaFolder(File folderName) throws IOException {
        for (File file : folderName.listFiles()) {
            if (file.isDirectory()) {
                importMirandaFolder(file);
            }
            String fileName = file.getName().toLowerCase();
            if (fileName.endsWith(".ini")) {
                log.info("Import Miranda from ini : " + file);
                importMirandaDbEditorIni.doImport(file);
            } else if (fileName.endsWith(".dat")) {
                log.info("Import Miranda from dat : " + file);
                importMirandaDb.doImport(file, false);
            } else {
                log.warn("Unknown miranda file : " + file);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new ComplexImport().doImport();
        
/*
        // todo [!] implement
        SHUTDOWN
        db.ExecSQL("SHUTDOWN");
*/
    }
}
