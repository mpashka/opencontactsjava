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

import org.mpn.contacts.framework.db.DataSource;
import org.mpn.contacts.framework.db.Row;
import org.mpn.contacts.framework.ui.AbstractDataSourceTableModel;

import javax.swing.*;

/**
 * todo [!] Create javadocs for org.mpn.contacts.framework.ui.dnd.DndTable here
 *
 * @author <a href="mailto:pmoukhataev@jnetx.ru">Pavel Moukhataev</a>
 * @version $Revision$
 */
public abstract class AbstractDndTable extends AbstractDataSourceTableModel implements RowExportable {

    private JTable jTable;

    public AbstractDndTable(DataSource dataSource) {
        super(dataSource);
        jTable = new JTable(this);
        jTable.setDragEnabled(true);
        jTable.setTransferHandler(RowTransferHandler.INSTANCE);
        DragMouseAdapter.addDragSupport(jTable);
    }

    public JTable getJTable() {
        return jTable;
    }

    public Row exportRow() {
        int index = jTable.getSelectedRow();
        return index >= 0 ? getRow(index) : null;
    }

}
