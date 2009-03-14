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

import java.io.File;
import java.io.IOException;

/**
 * todo [!] Create javadocs for org.mpn.contacts.importer.ExportTest here
 *
 * @author <a href="mailto:pmoukhataev@jnetx.ru">Pavel Moukhataev</a>
 * @version $Revision$
 */
public class ExportTest {

    static final Logger log = Logger.getLogger("slee.ExportTest");
    private static final String JABBER_ICQ_GATEWAY = "icq.gornyak.net";


    @Test
    public void exportGmail() throws IOException {
        new ExportGmailCsv().doExportGmail(new File("target/gmail.csv"), new String[] {
                JABBER_ICQ_GATEWAY
//                , "icq2.mo.pp.ru"
           }
        );
//        new ExportGmailCsv().doExportGmail(new File("target"), 50);
//        new ExportGmail().doExportGmail(new File("target"), 10);
    }

    @Test
    public void exportGmailWWW() throws Exception {
        TestProperties testProperties = TestProperties.getInstance();
        new ExportGmailWww().doExportGmail(testProperties.getProperty("google.login.production"), testProperties.getProperty("google.password.production"));

    }

    @Test
    public void exportJabber() throws Exception {
        TestProperties testProperties = TestProperties.getInstance();
        new ExportJabber().doExport(testProperties.getProperty("google.login.production"), testProperties.getProperty("google.password.production"), JABBER_ICQ_GATEWAY);

    }
}
