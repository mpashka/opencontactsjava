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
import org.mpn.contacts.framework.ui.MDIDesktopPane;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.util.Map;
import java.util.HashMap;
import java.awt.*;

/**
 * todo [!] Create javadocs for org.mpn.contacts.framework.ui.dnd.DndFrame here
 *
 * @author <a href="mailto:pmoukhataev@jnetx.ru">Pavel Moukhataev</a>
 * @version $Revision$
 */
public class DndFrame extends JInternalFrame implements RowImportable {
    static final Logger log = Logger.getLogger(DndFrame.class);

    private static final int xOffset = 10, yOffset = 10;

    private MDIDesktopPane desktop = new MDIDesktopPane();

    private Map<DataSource, EmptyDndTable> tables = new HashMap<DataSource, EmptyDndTable>();

    public DndFrame(String title) {
        super(title);
        setMinimumSize(new Dimension(300, 200));
        setTransferHandler(RowTransferHandler.INSTANCE);
        setContentPane(new JScrollPane(desktop));
    }

    public void importRow(Row row) {
        log.debug("Importing row to dnd frame : " + row);
        DataSource dataSource = row.getDataSource();
        EmptyDndTable table = tables.get(dataSource);
        if (table == null) {
            table = new EmptyDndTable(dataSource);
            tables.put(dataSource, table);
            int count = tables.size();
            JInternalFrame jTable = createTableFrame(dataSource, table.getJTable());
            jTable.setLocation(count * xOffset, count * yOffset);

            desktop.add(jTable);
            log.debug("New table created : " + table);
        }
        table.addRow(row);
    }

    private JInternalFrame createTableFrame(DataSource dataSource, JTable jTable) {
        JInternalFrame internalFrame = new JInternalFrame(dataSource.getName(), true, true, true, true);

        internalFrame.getContentPane().add(new JScrollPane(jTable), BorderLayout.CENTER);
        internalFrame.setMinimumSize(new Dimension(100, 50));
        internalFrame.setSize(new Dimension(150, 100));
        internalFrame.setVisible(true);

        return internalFrame;
    }
}
