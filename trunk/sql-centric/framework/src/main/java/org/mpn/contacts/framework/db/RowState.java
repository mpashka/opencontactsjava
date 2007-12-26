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
package org.mpn.contacts.framework.db;

/**
 * todo [!] Create javadocs for org.mpn.contacts.framework.db.RowState here
 *
 * @author <a href="mailto:pmoukhataev@jnetx.ru">Pavel Moukhataev</a>
 * @version $Revision$
 */
public enum RowState {
    empty {
        public void commit(Row row) {}
    },

    unmodified {
        public void commit(Row row) {}
    },

    update {
        public void commit(Row row) {
            row.commitUpdate();
        }
    },

    add {
        public void commit(Row row) {
            row.commitInsert();
        }
    }

    ;

    public abstract void commit(Row row);
}
