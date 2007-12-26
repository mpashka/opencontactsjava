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
package org.mpn.contacts.framework.db;

import org.mpn.contacts.framework.EventGeneratorBase;
import org.mpn.contacts.framework.EventListener;
import org.mpn.contacts.framework.Value;

/**
 * todo [!] Create javadocs for org.mpn.contacts.framework.db.RowValue here
 *
 * @author <a href="mailto:pmoukhataev@jnetx.ru">Pavel Moukhataev</a>
 * @version $Revision$
 */
public class RowValue<Type> extends EventGeneratorBase<Value<Type>> implements Value<Type>, EventListener<Row> {
    private Row row;
    private Field<Type> field;

    public RowValue(Row row, Field<Type> field) {
        this.row = row;
        this.field = field;

        row.addListener(this);
    }

    public Type getData() {
        return row.getData(field);
    }

    public void setData(Type data) {
        row.setData(field, data);
    }

    public void onEvent(Row source) {
        notifyListeners(this);
    }
}
