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
 * todo [!] Create javadocs for org.mpn.contacts.framework.ui.UiIntegerField here
 *
 * @author <a href="mailto:pmoukhataev@jnetx.ru">Pavel Moukhataev</a>
 * @version $Revision$
 */
public class UiIntegerField extends UiComponent<Integer, JTextField> {

    public JTextField createUiComponent() {
        JTextField jTextField = new JTextField();
        jTextField.setDragEnabled(true);
        jTextField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                notifyListeners(UiIntegerField.this);
            }

            public void removeUpdate(DocumentEvent e) {
                notifyListeners(UiIntegerField.this);
            }

            public void changedUpdate(DocumentEvent e) {
                notifyListeners(UiIntegerField.this);
            }
        });
        return jTextField;
    }

    public Integer getData() {
        return Integer.parseInt(uiComponent.getText());
    }

    public void setData(Integer data) {
        uiComponent.setText(String.valueOf(data == null ? getDefaultValue() : data));
    }

    public Integer getDefaultValue() {
        return 0;
    }

}
