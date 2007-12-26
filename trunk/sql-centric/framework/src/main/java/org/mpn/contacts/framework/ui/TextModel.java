package org.mpn.contacts.framework.ui;

import org.apache.log4j.Logger;
import org.mpn.contacts.framework.db.Field;
import org.mpn.contacts.framework.db.Row;

/**
 * @author <a href="mailto:pmoukhataev@dev.java.net">Pavel Moukhataev</a>
 * @version $Id$
 */
public class TextModel {

    final static Logger LOG = Logger.getLogger(TextModel.class);

    private Row row;
    private Field<String> field;

    public TextModel(Row row, Field<String> field) {
        this.row = row;
        this.field = field;
    }
}
