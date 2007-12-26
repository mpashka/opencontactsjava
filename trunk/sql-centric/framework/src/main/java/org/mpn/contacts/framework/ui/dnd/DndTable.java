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

import javax.swing.*;
import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableModel;
import java.util.List;
import java.util.ArrayList;
import java.awt.*;

/**
 * todo [!] Create javadocs for org.mpn.contacts.framework.ui.dnd.DndTable here
 *
 * @author <a href="mailto:pmoukhataev@jnetx.ru">Pavel Moukhataev</a>
 * @version $Revision$
 */
public class DndTable extends JInternalFrame implements TableModel, RowExportable, RowImportable {

    private DataSource dataSource;
    private List<Row> rows = new ArrayList<Row>();
    private List<TableModelListener> tableModelListeners = new ArrayList<TableModelListener>();

    private JTable table;

    public DndTable(DataSource dataSource) {
        super(dataSource.getName(), true, true, true, true);
        this.dataSource = dataSource;
        table = new JTable(this);
        table.setDragEnabled(true);
        table.setTransferHandler(RowTransferHandler.INSTANCE);
        DragMouseAdapter.addDragSupport(table);

        getContentPane().add(new JScrollPane(table), BorderLayout.CENTER);
        setMinimumSize(new Dimension(100, 50));
        setSize(new Dimension(150, 100));
        setVisible(true);
    }

    public Row exportRow() {
        int index = table.getSelectedRow();
        return index >= 0 ? rows.get(index) : null;
    }

    public void importRow(Row row) {
        if (!row.getDataSource().equals(dataSource)) {
            JOptionPane.showMessageDialog(this, "Row data source " + row.getDataSource() + " is not equals to" +
                    " this table data source " + dataSource);
        } else {
            addRow(row);
        }
    }

    public void addRow(Row row) {
        rows.add(row);
        TableModelEvent e = new TableModelEvent(this, rows.size() - 1, rows.size() - 1, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT);
        for (TableModelListener tableModelListener : tableModelListeners) {
            tableModelListener.tableChanged(e);
        }
    }

    public int getRowCount() {
        return rows.size();
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
        Row row = rows.get(rowIndex);
        return row.getData()[columnIndex];
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        throw new UnsupportedOperationException();
    }

    public void addTableModelListener(TableModelListener l) {
        tableModelListeners.add(l);
    }

    public void removeTableModelListener(TableModelListener l) {
        tableModelListeners.remove(l);
    }

}
