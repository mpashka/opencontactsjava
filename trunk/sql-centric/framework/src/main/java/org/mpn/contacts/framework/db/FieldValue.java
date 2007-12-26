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

import org.mpn.contacts.framework.Value;

/**
 * todo [!] Create javadocs for org.mpn.contacts.framework.db.FieldValue here
 *
 * @author <a href="mailto:pmoukhataev@jnetx.ru">Pavel Moukhataev</a>
 * @version $Revision$
 */
public class FieldValue<Type> {
    private Field<Type> field;
    private Value<Type> value;

    public FieldValue(Field<Type> field, Value<Type> value) {
        this.field = field;
        this.value = value;
    }

    public Field<Type> getField() {
        return field;
    }

    public Value<Type> getValue() {
        return value;
    }
}
