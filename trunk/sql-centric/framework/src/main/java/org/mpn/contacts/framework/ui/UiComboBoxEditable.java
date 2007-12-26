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
import org.mpn.contacts.framework.db.Field;
import org.mpn.contacts.framework.db.Row;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * todo [!] Create javadocs for org.mpn.contacts.framework.ui.UiComboBoxEditable here
 *
 * @author <a href="mailto:pmoukhataev@jnetx.ru">Pavel Moukhataev</a>
 * @version $Revision$
 */
public class UiComboBoxEditable extends UiComponent<Long, JComboBox> {

    public UiComboBoxEditable(DataSource referTable, Field<Long> referIdField, Field<String> referStringField) {
        for (Row referTableRow : referTable) {
            Long id = referTableRow.getData(referIdField);
            String text = referTableRow.getData(referStringField);
            uiComponent.addItem(new UiComboBoxItem(id, text));
        }
    }

    public JComboBox createUiComponent() {
        JComboBox comboBox = new JComboBox();
        comboBox.setEditable(true);
        comboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                notifyListeners(UiComboBoxEditable.this);
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
