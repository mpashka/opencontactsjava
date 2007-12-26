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

import org.apache.log4j.Logger;
import org.mpn.contacts.framework.db.Row;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Arrays;

/**
 * todo [!] Create javadocs for org.mpn.contacts.framework.ui.dnd.RowTransferHandler here
 *
 * @author <a href="mailto:pmoukhataev@jnetx.ru">Pavel Moukhataev</a>
 * @version $Revision$
 */
public class RowTransferHandler extends TransferHandler {

    static final Logger log = Logger.getLogger(RowTransferHandler.class);

    public static final DataFlavor ROW_DATA_FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=" + Row.class.getName()
            , null);
    private static final DataFlavor[] ROW_DATA_FLAVORS = {ROW_DATA_FLAVOR};

    public static RowTransferHandler INSTANCE = new RowTransferHandler();

    public boolean importData(JComponent c, Transferable t) {
        log.debug("Importing data to " + c);
        try {
            RowImportable importable = (RowImportable) c;
            importable.importRow((Row) t.getTransferData(ROW_DATA_FLAVOR));
            return true;
        } catch (Exception e) {
            log.error("Error importing data", e);
            return false;
        }
    }

    public boolean canImport(JComponent c, DataFlavor[] flavors) {
//        log.debug("Can import to " + c);
        return c instanceof RowImportable && Arrays.asList(flavors).contains(ROW_DATA_FLAVOR);
    }

    protected Transferable createTransferable(JComponent c) {
        log.debug("Create transferrable for " + c);
        if (c instanceof RowExportable) {
            RowExportable exportable = (RowExportable) c;
            Row row = exportable.exportRow();
            return row != null ? new RowTransferable(row) : null;
        }
        return null;
    }


     public int getSourceActions(JComponent c) {
        return COPY_OR_MOVE;
    }


    public class RowTransferable implements Transferable {
        private Row row;

        public RowTransferable(Row row) {
            this.row = row;
        }

        public DataFlavor[] getTransferDataFlavors() {
            return ROW_DATA_FLAVORS;
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavor.equals(ROW_DATA_FLAVOR);
        }

        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (flavor.equals(ROW_DATA_FLAVOR)) {
                return row;
            } else {
                throw new UnsupportedFlavorException(flavor);
            }
        }
    }
}
