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

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.*;

/**
 * todo [!] Create javadocs for org.mpn.contacts.framework.ui.dnd.DragMouseAdapter here
 *
 * @author <a href="mailto:pmoukhataev@jnetx.ru">Pavel Moukhataev</a>
 * @version $Revision$
 */
class DragMouseAdapter extends MouseAdapter {
    public void mousePressed(MouseEvent e) {
        JComponent c = (JComponent)e.getSource();
        TransferHandler handler = c.getTransferHandler();
        handler.exportAsDrag(c, e, TransferHandler.COPY);
    }

    public static void addDragSupport(Component component) {
        component.addMouseListener(new DragMouseAdapter());
    }
}
