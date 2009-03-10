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
package org.mpn.contacts.importer.google;

import com.sun.mail.imap.IMAPFolder;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.mpn.contacts.importer.TestProperties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import java.util.Properties;

/**
 * todo [!] Create javadocs for org.mpn.contacts.importer.google.TestGmailImap here
 *
 * @author <a href="mailto:pmoukhataev@jnetx.ru">Pavel Moukhataev</a>
 * @version $Revision$
 */
public class TestGmailImap {

    static final Logger log = Logger.getLogger("slee.TestGmailImap");

    @Test
    public void testGmailFetch() throws Exception {
        TestProperties testProperties = TestProperties.getInstance();

        String host = "imap.gmail.com";
        String username = testProperties.getGoogleLogin();
        String password = testProperties.getGooglePassword();
        int port = 993;

// Create empty properties
        Properties props = new Properties();
        props.setProperty("mail.store.protocol", "imaps");
        props.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.setProperty("mail.imap.socketFactory.fallback", "false");
        props.setProperty("mail.imap.host", "imap.gmail.com");
        props.setProperty("mail.imap.port", "993");
        props.setProperty("mail.imap.connectiontimeout", "5000");
        props.setProperty("mail.imap.timeout", "5000");

// Get session
        Session session = Session.getDefaultInstance(props, null);

// Get the store
//        URLName urlName = new URLName("imap://MYUSERNAME@gmail.com:MYPASSWORD@imap.gmail.com");
        Store store = session.getStore("imap");
        store.connect(host, port, username, password);

        dumpFolder(store.getDefaultFolder(), true, "");

        {
// Get folder
//        Folder folder = store.getFolder("INBOX");
        Folder folder = store.getFolder("[Gmail]/All Mail");
        folder.open(Folder.READ_ONLY);

// Get directory
        Message message[] = folder.getMessages();

        for (int i=0, n=message.length; i<n; i++) {
           System.out.println(i + ": " + message[i].getFrom()[0]
             + "\t" + message[i].getSubject());
        }

// Close connection
        folder.close(false);
        store.close();
        }



    }

    static boolean verbose = true;
    static void dumpFolder(Folder folder, boolean recurse, String tab)
                                        throws Exception {
        System.out.println(tab + "Name:      " + folder.getName());
        System.out.println(tab + "Full Name: " + folder.getFullName());
        System.out.println(tab + "URL:       " + folder.getURLName());

        if (verbose) {
            if (!folder.isSubscribed())
                System.out.println(tab + "Not Subscribed");

            if ((folder.getType() & Folder.HOLDS_MESSAGES) != 0) {
                if (folder.hasNewMessages())
                    System.out.println(tab + "Has New Messages");
                System.out.println(tab + "Total Messages:  " +
                                                folder.getMessageCount());
                System.out.println(tab + "New Messages:    " +
                                                folder.getNewMessageCount());
                System.out.println(tab + "Unread Messages: " +
                                                folder.getUnreadMessageCount());
            }

            if ((folder.getType() & Folder.HOLDS_MESSAGES) != 0) {
                System.out.println(tab + "List messages...");
                folder.open(Folder.READ_ONLY);

// Get directory
                Message message[] = folder.getMessages();

                for (int i=0, n=message.length; i<n; i++) {
                    System.out.println(tab + "   -> " + i + ": " + message[i].getFrom()[0]
                            + "\t" + message[i].getSubject());
                }
            }

// Close connection
//            folder.close(false);


            if ((folder.getType() & Folder.HOLDS_FOLDERS) != 0)
                System.out.println(tab + "Is Directory");

            /*
             * Demonstrate use of IMAP folder attributes
             * returned by the IMAP LIST response.
             */
            if (folder instanceof IMAPFolder) {
                IMAPFolder f = (IMAPFolder)folder;
                String[] attrs = f.getAttributes();
                if (attrs != null && attrs.length > 0) {
                    System.out.println(tab + "IMAP Attributes:");
                    for (int i = 0; i < attrs.length; i++)
                        System.out.println(tab + "    " + attrs[i]);
                }
            }
        }

        System.out.println();

        if ((folder.getType() & Folder.HOLDS_FOLDERS) != 0) {
            if (recurse) {
                Folder[] f = folder.list();
                for (int i = 0; i < f.length; i++)
                    dumpFolder(f[i], recurse, tab + "    ");
            }
        }
    }


}
