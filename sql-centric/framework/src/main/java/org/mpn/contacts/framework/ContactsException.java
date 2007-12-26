package org.mpn.contacts.framework;

import org.apache.log4j.Logger;

/**
 * @author <a href="mailto:pmoukhataev@dev.java.net">Pavel Moukhataev</a>
 * @version $Id$
 */
public class ContactsException extends RuntimeException {

    public ContactsException() {
    }

    public ContactsException(String message) {
        super(message);
    }

    public ContactsException(String message, Throwable cause) {
        super(message, cause);
    }

    public ContactsException(Throwable cause) {
        super(cause);
    }
}
