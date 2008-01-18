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

/**
 * todo [!] Create javadocs for org.mpn.contacts.framework.db.DataSource here
 *
 * @author <a href="mailto:pmoukhataev@jnetx.ru">Pavel Moukhataev</a>
 * @version $Revision$
 */
public interface DataSource extends Iterable<Row> {

    void addListener(DataSourceListener listener);
    void removeListener(DataSourceListener listener);

    Field<Long> getIdField();

    String getName();

    Field[] getFieldsMetaData();

    int getFieldIndex(Field field);

    Row getRow();

//    DbTableRow getDefaultRow();

    /**
     * Add new row into table, return index of newly inserted row
     * @param fieldsData row data
     * @return index of newly inserted row
     */
    int commitInsert(Object[] fieldsData);

    void commitUpdate(Object[] fieldsData);

    void commitDelete(Object[] fieldsData);

    int getRowCount();

    Object[] getRowData(int index);

    Row getOriginalRow(int index);

    void closeRow(Row row);
}
