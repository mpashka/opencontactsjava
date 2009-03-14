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
package org.mpn.contacts.framework.ui;

import org.mpn.contacts.framework.db.DataSource;
import org.mpn.contacts.framework.db.Row;

import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * todo [!] Create javadocs for org.mpn.contacts.framework.ui.dnd.DndTable here
 *
 * @author <a href="mailto:pmoukhataev@jnetx.ru">Pavel Moukhataev</a>
 * @version $Revision$
 */
public abstract class AbstractDataSourceTableModel implements TableModel {

    private DataSource dataSource;
    private List<TableModelListener> tableModelListeners = new ArrayList<TableModelListener>();

    public AbstractDataSourceTableModel(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public int getColumnCount() {
        return dataSource.getFieldsMetaData().length;
    }

    public String getColumnName(int columnIndex) {
        return dataSource.getFieldsMetaData()[columnIndex].getName();
    }

    public Class<?> getColumnClass(int columnIndex) {
        return dataSource.getFieldsMetaData()[columnIndex].getTypeClass();
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        return dataSource.getRowData(rowIndex)[columnIndex];
    }

    public abstract Row getRow(int rowIndex);

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        throw new UnsupportedOperationException();
    }

    public void addTableModelListener(TableModelListener l) {
        tableModelListeners.add(l);
    }

    public void removeTableModelListener(TableModelListener l) {
        tableModelListeners.remove(l);
    }

    protected void notifyTableChanged(int firstRow, int lastRow, int type) {
        TableModelEvent e = new TableModelEvent(this, firstRow, lastRow, TableModelEvent.ALL_COLUMNS, type);
        for (TableModelListener tableModelListener : tableModelListeners) {
            tableModelListener.tableChanged(e);
        }
    }


}
