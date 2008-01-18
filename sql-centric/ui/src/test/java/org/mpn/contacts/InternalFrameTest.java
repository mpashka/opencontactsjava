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
package org.mpn.contacts;

import junit.framework.TestCase;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;

/**
 * todo [!] Create javadocs for org.mpn.contacts.InternalFrameTest here
 *
 * @author <a href="mailto:pmoukhataev@jnetx.ru">Pavel Moukhataev</a>
 * @version $Revision$
 */
public class InternalFrameTest extends TestCase {

    static final Logger log = Logger.getLogger(InternalFrameTest.class);

    public void testLayout() throws InterruptedException {
        JInternalFrame userDetailsWindow = new JInternalFrame("User data", true, true, true, true);

        log.debug("Layout : " + userDetailsWindow.getLayout());
        log.debug("Content Pane Layout : " + userDetailsWindow.getContentPane().getLayout());
        log.debug("Content Pane : " + userDetailsWindow.getContentPane());

//        userDetailsWindow.getContentPane().setLayout(new BorderLayout());

        userDetailsWindow.add(new JTextArea("aaa"), BorderLayout.SOUTH);
        userDetailsWindow.getContentPane().add(new JTextField("bbb"), BorderLayout.CENTER);

        JDesktopPane desktopPane = new JDesktopPane();
        desktopPane.add(userDetailsWindow);
        userDetailsWindow.pack();
        userDetailsWindow.setVisible(true);

        JFrame frame = new JFrame("Test frame");
        frame.setContentPane(desktopPane);
        frame.setSize(400, 300);
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        printLayoutTree("", frame);

        Thread.sleep(200000);
    }

    private void printLayoutTree(String ident, Component component) {
        Component[] components = null;
        if (component instanceof Container) {
            Container container = (Container) component;
            components = container.getComponents();
        }
        if (components != null && components.length > 0) {
            log.debug(ident + "[Container] " + component);
            for (Component component1 : components) {
                printLayoutTree(ident + "    ", component1);
            }
        } else {
            log.debug(ident + component);
        }
    }
}
