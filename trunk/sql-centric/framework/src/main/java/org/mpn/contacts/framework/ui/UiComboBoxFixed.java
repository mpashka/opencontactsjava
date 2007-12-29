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

import org.mpn.contacts.framework.EventListener;
import org.mpn.contacts.framework.db.DataSource;
import org.mpn.contacts.framework.db.Field;
import org.mpn.contacts.framework.db.Row;

/**
 * todo [!] Create javadocs for org.mpn.contacts.framework.ui.UiComboBoxEditable here
 *
 * @author <a href="mailto:pmoukhataev@jnetx.ru">Pavel Moukhataev</a>
 * @version $Revision$
 */
public class UiComboBoxFixed extends UiComboBox {

    private DataSource referTable;
    private Field<Long> referIdField;
    private Field<String> referStringField;

    public UiComboBoxFixed(DataSource referTable, Field<Long> referIdField, Field<String> referStringField) {
        this.referTable = referTable;
        this.referIdField = referIdField;
        this.referStringField = referStringField;

        referTable.addListener(new EventListener<DataSource>() {
            public void onEvent(DataSource source) {
                readData();
            }
        });
        readData();
    }

    private void readData() {
        uiComponent.removeAllItems();
        for (Row referTableRow : referTable) {
            Long id = referTableRow.getData(referIdField);
            String text = referTableRow.getData(referStringField);
            uiComponent.addItem(new UiComboBoxItem(id, text));
        } 
    }

}
