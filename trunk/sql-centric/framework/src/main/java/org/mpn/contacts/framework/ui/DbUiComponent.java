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

import org.apache.log4j.Logger;
import org.mpn.contacts.framework.db.Field;
import org.mpn.contacts.framework.db.Row;

/**
 * This class stores information about one ui component
 */
public class DbUiComponent<DataType> {
    static final Logger log = Logger.getLogger(DbUiComponent.class);

    private UiComponent<DataType, ?> uiComponent;
    private Field<DataType> dbFieldMetadata;
    private Row row;

    public DbUiComponent(UiComponent<DataType, ?> uiComponent, Field<DataType> dbFieldMetadata, Row row) {
        this.uiComponent = uiComponent;
        this.row = row;
        this.dbFieldMetadata = dbFieldMetadata;
    }

    public DataType getDbData() {
        return row.getData(dbFieldMetadata);
    }

    public void setDbData(DataType data) {
        row.setData(dbFieldMetadata, data);
    }

    public UiComponent<DataType, ?> getUiComponent() {
        return uiComponent;
    }

    public void commit() {
        setDbData(uiComponent.getData());
    }

    public void update() {
        uiComponent.setData(row.hasData() ? getDbData() : uiComponent.getDefaultValue());
    }

    public void clear() {
        uiComponent.clear();
    }

    public boolean isDirty() {
        DataType obj1 = uiComponent.getData();
        DataType obj2 = getDbData();
        return obj1 == null ? obj2 == null : (obj2 != null && obj1.equals(obj2));
    }

}
