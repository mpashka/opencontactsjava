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
import org.mpn.contacts.framework.db.DataSourceListener;
import org.mpn.contacts.framework.db.Row;

import javax.swing.event.TableModelEvent;

/**
 * todo [!] Create javadocs for org.mpn.contacts.framework.ui.dnd.DataSourceTable here
 *
 * @author <a href="mailto:pmoukhataev@jnetx.ru">Pavel Moukhataev</a>
 * @version $Revision$
 */
public class DataSourceTable extends AbstractDndTable implements DataSourceListener {

    private Row row;

    public DataSourceTable(DataSource dataSource) {
        super(dataSource);
        row = getDataSource().getRow();

        dataSource.addListener(this);
    }

    public void onInsert(int rowIndex, Object[] fieldsData) {
        notifyTableChanged(rowIndex, rowIndex, TableModelEvent.INSERT);
    }

    public void onUpdate(int rowIndex, Object[] fieldsData) {
        notifyTableChanged(rowIndex, rowIndex, TableModelEvent.UPDATE);
    }

    public void onDelete(int rowIndex, Object[] fieldsData) {
        notifyTableChanged(rowIndex, rowIndex, TableModelEvent.DELETE);
    }

    public Row getRow(int rowIndex) {
        row.setIndex(rowIndex);
        return row;
    }

    public int getRowCount() {
        return getDataSource().getRowCount();
    }
}
