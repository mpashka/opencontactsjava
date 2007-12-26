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
package org.mpn.contacts.framework.ui.dnd;

import org.mpn.contacts.framework.db.Row;

import javax.swing.*;

/**
 * todo [!] Create javadocs for org.mpn.contacts.framework.ui.dnd.RowPanel here
 *
 * @author <a href="mailto:pmoukhataev@jnetx.ru">Pavel Moukhataev</a>
 * @version $Revision$
 */
public class RowPanel extends JPanel implements RowExportable {
    private Row row;

    public RowPanel(Row row) {
        this.row = row;
//        setDra
        setTransferHandler(RowTransferHandler.INSTANCE);
        DragMouseAdapter.addDragSupport(this);
    }

    public Row exportRow() {
        return row.getOriginalRow();
    }
}
