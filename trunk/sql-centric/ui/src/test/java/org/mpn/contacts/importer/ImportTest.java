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
import org.junit.Test;

import java.io.IOException;
import java.io.File;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * todo [!] Create javadocs for org.mpn.contacts.importer.ImportTest here
 *
 * @author <a href="mailto:pmoukhataev@jnetx.ru">Pavel Moukhataev</a>
 * @version $Revision$
 */
public class ImportTest {

    static final Logger log = Logger.getLogger(ImportTest.class);

    File rootImportDir = new File("../.#data/import");
    ImportLdap importLdap = new ImportLdap();
    ImportHtml importHtml = new ImportHtml();
    ImportCsv importCsv = new ImportCsv("Csv", false);
    ImportVCard importVCard = new ImportVCard();
    ImportGmail importGmail = new ImportGmail();
    ImportPalmDesktop importPalmDesktop = new ImportPalmDesktop();
    private ImportMirandaDb importMirandaDb = new ImportMirandaDb();
    private ImportMirandaDbEditorIni importMirandaDbEditorIni = new ImportMirandaDbEditorIni();
    ImportRegexpBase importRegexpBase = new ImportRegexpBase("Regex", true);
    Map<String, String> definedCompanyName = new HashMap<String, String>();

    @Test
    public void doImport() throws IOException {
        importLdap1();

        importHtml();

        importLdap2();

        importCsvArtezio();


        importVCard();

        importRegex();

        importGmail();

        importMirandaFolder();


        importCsvTheBat();

        importRegexpBase.doImport(null, new File(rootImportDir, "csv/e-mail.contacts2.2006-04-11.csv"),
                Pattern.compile("^([^;]+) ;(.+)$"), new String[]{"fullName", "messagingEmail"});
        importPalm();


        importVCards();
    }

    @Test
    public void importVCards() throws IOException {
        importVCard.doImportVCards(new File(rootImportDir, "vcard/vcard-MobileContacts-ValueCommerce-2005-02-07"));
        importVCard.doImportVCard(new File(rootImportDir, "vcard/voipcheap-contacts.2007-12-04.vcf"));
    }

    @Test
    public void importPalm() throws IOException {
        // This is palm desktop contacts
//        importCsv.doImport(new File(rootImportDir, "excel\\contacts.2006-04-14.csv"), new HashMap<String, String>());
//        importCsv.doImport(new File(rootImportDir, "excel\\contacts.memo.2006-04-14.csv"), new HashMap<String, String>());

        importPalmDesktop.doImport(new File(rootImportDir, "PalmDesktop/СтарыеКонтактыPalmDesktop.2006-04-11.csv"));
        importPalmDesktop.doImport(new File(rootImportDir, "PalmDesktop/2006-04-13/contacts-comma.csv"));
//        importCsv.doImport(new File(rootImportDir, "PalmDesktop/2006-04-13/excel/contacts.memo.2006-04-14.csv"), new HashMap<String, String>());
    }



    public void importCsvTheBat() throws IOException {
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
    }

    public void importGmail() {
        importGmail.doImportFolder(new File(rootImportDir, "gmail"));
    }

    @Test
    public void importRegex() throws IOException {
        definedCompanyName.put("company", "artezio");
        importRegexpBase.doImport(definedCompanyName, new File(rootImportDir, "csv/artezio-list.2004-01-15.txt"),
                Pattern.compile("^(.+)$"), new String[] {"fullName"});

        definedCompanyName.put("company", "vc");
        importRegexpBase.doImport(definedCompanyName, new File(rootImportDir, "csv/vc-list.2004-01-15.txt"),
                Pattern.compile("^(.+)$"), new String[] {"fullName"});


        importRegexpBase.setImportCompany(false);
        importRegexpBase.doStreamImport(Pattern.compile(",[\\s]*"), null, new File(rootImportDir, "csv/e-mail.contacts.2005-09-21.txt"),
                Pattern.compile("\\\"?([^<]*)[\" ]*<([^>]+)>"), new String[]{"fullName", "messagingEmail"});
    }

    @Test
    public void importVCard() throws IOException {
        importVCard.doImportVCard(new File(rootImportDir, "csv/Mobile Phone Manager.2005-03-29/vcard.txt"));
    }

    @Test
    public void importCsvArtezio() throws IOException {
        definedCompanyName.put("company", "artezio");
        importRegexpBase.doImport(definedCompanyName, new File(rootImportDir, "csv/artezio-2002-07-23.txt"),
                Pattern.compile("^([^<]+) *<(([\\p{Lower}]+)@artezio.ru)>[, -]*(.*)$"),
                new String[] {
                        "fullName", "messagingEmail", "nick", "about"
                });

        Map<String, String> mobileContactsMapping = new HashMap<String, String>();
        mobileContactsMapping.put("Фамилия", "fullName");
        mobileContactsMapping.put("Телефон", "messagingMobile");
        importCsv.setWarningUnknownFields(false);
        // todo [!] add valuecommerce company here
        importCsv.doImport(new File(rootImportDir, "csv/MobileContacts-ValueCommerce-2005-02-07.txt"), mobileContactsMapping);
    }

    public void importLdap2() throws IOException {
        importLdap.doImport("vc", new File(rootImportDir, "ldap/vc.2005-07-07/japan.ldif"));
        importLdap.doImport("vc", new File(rootImportDir, "ldap/vc.2005-07-07/moscow.ldif"));
        importLdap.doImport("vc", new File(rootImportDir, "ldap/vc.2005-07-07/russia-all.ldif"));
    }

    public void importHtml() throws IOException {
        importHtml.doImportFolder("jnetx", new File(rootImportDir, "html/2007-12-21"));
    }

    public void importLdap1() throws IOException {
        importLdap.doImport("jnetx", new File(rootImportDir, "ldap/jnetx-addrs.2005-10-27.ldif"));
        importLdap.doImport("jnetx", new File(rootImportDir, "ldap/jnetx.adress-book.2007-02-01.ldif"));
        importLdap.doImport("jnetx", new File(rootImportDir, "ldap/jnetx.2007-03-26.ldif"));
        importLdap.doImport("jnetx", new File(rootImportDir, "ldap/jnetx-users.2008-01-23.ldif"));
    }

    @Test
    public void importMirandaFolder() throws IOException {
        importMirandaFolder(new File(rootImportDir, "Miranda"));
    }

    @Test
    public void testImportMirandaFile() throws IOException {
        importMirandaDbEditorIni.doImport(new File(rootImportDir, "Miranda/2006-04-11/contacs.miranda.ini"));
    }


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
                importMirandaDb.doImport(file);
            } else {
                log.warn("Unknown miranda file : " + file);
            }
        }
    }


}
