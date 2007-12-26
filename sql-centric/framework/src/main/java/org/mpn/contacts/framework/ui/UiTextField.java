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

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * todo [!] Create javadocs for org.mpn.contacts.framework.ui.UiTextField here
 *
 * @author <a href="mailto:pmoukhataev@jnetx.ru">Pavel Moukhataev</a>
 * @version $Revision$
 */
public class UiTextField extends UiComponent<String, JTextField> {

    public JTextField createUiComponent() {
        JTextField jTextField = new JTextField();
        jTextField.setDragEnabled(true);
        jTextField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                notifyListeners(UiTextField.this);
            }

            public void removeUpdate(DocumentEvent e) {
                notifyListeners(UiTextField.this);
            }

            public void changedUpdate(DocumentEvent e) {
                notifyListeners(UiTextField.this);
            }
        });
        return jTextField;
    }

    public String getData() {
        return uiComponent.getText();
    }

    public void setData(String data) {
        uiComponent.setText(data);
    }

    public String getDefaultValue() {
        return "";
    }

}
