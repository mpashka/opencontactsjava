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
package org.mpn.contacts.framework.db.filter;

import org.mpn.contacts.framework.EventGeneratorBase;
import org.mpn.contacts.framework.EventListener;
import org.mpn.contacts.framework.Value;
import org.mpn.contacts.framework.db.*;

/**
 * todo [!] Create javadocs for org.mpn.contacts.framework.db.filter.FixedValueFilter here
 *
 * @author <a href="mailto:pmoukhataev@jnetx.ru">Pavel Moukhataev</a>
 * @version $Revision$
 */
public final class FixedValueFilter<Type> extends EventGeneratorBase<Filter> implements Filter, EventListener<Value<Type>>  {
    private Field<Type> field;
    private Value<Type> value;

    public FixedValueFilter(Field<Type> field, Value<Type> value) {
        this.field = field;
        this.value = value;
        value.addListener(this);
    }

    /**
     * Is called on value change, notify filter listeners that filter changed
     * @param source
     */
    public void onEvent(Value<Type> source) {
        notifyListeners(this);
    }

    public boolean acceptRow(Row row) {
        Type rowData = row.getData(field);
        Type valueData = value.getData();
        return rowData == null ? valueData == null : rowData.equals(valueData);
    }

}
