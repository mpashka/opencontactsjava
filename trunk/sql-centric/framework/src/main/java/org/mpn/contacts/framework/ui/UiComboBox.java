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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * todo [!] Create javadocs for org.mpn.contacts.framework.ui.UiComboBoxEditable here
 *
 * @author <a href="mailto:pmoukhataev@jnetx.ru">Pavel Moukhataev</a>
 * @version $Revision$
 */
public class UiComboBox extends UiComponent<Long, JComboBox> {

    public JComboBox createUiComponent() {
        JComboBox comboBox = new JComboBox();
        comboBox.setEditable(false);
        comboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                notifyListeners(UiComboBox.this);
            }
        });
        return comboBox;
    }

    public Long getData() {
        return ((UiComboBoxItem) uiComponent.getSelectedItem()).getId();
    }

    public void setData(Long data) {
        for (int i = 0; i < uiComponent.getItemCount(); i++) {
            UiComboBoxItem comboBoxItem = (UiComboBoxItem) uiComponent.getItemAt(i);
            if (comboBoxItem.getId().equals(data)) {
                uiComponent.setSelectedItem(comboBoxItem);
                return;
            }
        }
    }

    public Long getDefaultValue() {
        return 0l;
    }

}
