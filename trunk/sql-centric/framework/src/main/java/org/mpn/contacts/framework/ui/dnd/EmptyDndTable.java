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
import org.mpn.contacts.framework.db.DataSource;

import javax.swing.event.TableModelEvent;
import javax.swing.*;
import java.util.List;
import java.util.ArrayList;

/**
 * todo [!] Create javadocs for org.mpn.contacts.framework.ui.dnd.EmptyDndTable here
 *
 * @author <a href="mailto:pmoukhataev@jnetx.ru">Pavel Moukhataev</a>
 * @version $Revision$
 */
public class EmptyDndTable extends AbstractDndTable implements RowImportable {
    private List<Row> rows = new ArrayList<Row>();

    public EmptyDndTable(DataSource dataSource) {
        super(dataSource);
    }

    public void addRow(Row row) {
        rows.add(row);
        notifyTableChanged(rows.size() - 1, rows.size() - 1, TableModelEvent.INSERT);
    }

    public Row getRow(int rowIndex) {
        return rows.get(rowIndex);
    }

    public int getRowCount() {
        return rows.size();
    }

    public void importRow(Row row) {
        DataSource dataSource = getDataSource();
        if (!row.getOriginalRow().getDataSource().equals(dataSource)) {
            JOptionPane.showMessageDialog(getJTable(), "Row data source " + row.getDataSource() + " is not equals to" +
                    " this table data source " + dataSource);
        } else {
            addRow(row);
        }
    }

}
