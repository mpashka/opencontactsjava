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

import org.mpn.contacts.framework.EventListener;
import org.mpn.contacts.framework.EventGeneratorBase;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * todo [!] Create javadocs for org.mpn.contacts.framework.db.FilteredDataSource here
 *
 * @author <a href="mailto:pmoukhataev@jnetx.ru">Pavel Moukhataev</a>
 * @version $Revision$
 */
public class FilteredDataSource extends EventGeneratorBase<DataSource> implements DataSource {

    private DataSource dbTable;
    private Filter filter;
    private FieldValue[] defaultValues;
    private List<Integer> index2dbTableIndex;

    /**
     * todo [!] memory leak here. Weak refs must be used instead or another mechanism used.
     */
    private List<Row> rows = new ArrayList<Row>();

    public FilteredDataSource(DataSource dbTable, Filter filter, FieldValue... defaultValues) {
        this.dbTable = dbTable;
        this.filter = filter;
        this.defaultValues = defaultValues;
        filter.addListener(new EventListener<Filter>() {
            public void onEvent(Filter source) {
                readData();
                notifyDataChange();
            }
        });
        readData();
    }

    public void addListener(DataSourceListener listener) {
        dbTable.addListener(listener);
    }

    public void removeListener(DataSourceListener listener) {
        dbTable.removeListener(listener);
    }

    public Field<Long> getIdField() {
        return dbTable.getIdField();
    }

    public String getName() {
        return "Filtered:" + dbTable.getName();
    }

    /**
     * Is called on filter change and during construction
     */
    private void readData() {
        index2dbTableIndex = new ArrayList<Integer>();
        for (Row row : dbTable) {
            if (filter.acceptRow(row)) {
                index2dbTableIndex.add(row.getIndex());
            }
        }
    }

    private void notifyDataChange() {
        for (Row row1 : rows) {
            row1.update();
        }
        notifyListeners(FilteredDataSource.this);
    }

    public int getRowCount() {
        return index2dbTableIndex.size();
    }

    public Object[] getRowData(int index) {
        return dbTable.getRowData(index2dbTableIndex.get(index));
    }

    public Field[] getFieldsMetaData() {
        return dbTable.getFieldsMetaData();
    }

    public Row getRow() {
        DbTableRow dbTableRow = new DbTableRow(this, dbTable.getFieldsMetaData());
        rows.add(dbTableRow);
        return dbTableRow;
    }

    public Iterator<Row> iterator() {
        DbTableRow dbTableRow = new DbTableRow(this, dbTable.getFieldsMetaData());
        dbTableRow.startIteration();
        return dbTableRow;
    }

    public int commitInsert(Object[] fieldsData) {
        updateDefaultFields(fieldsData);

        int newIndex = dbTable.commitInsert(fieldsData);
        index2dbTableIndex.add(newIndex);
        return index2dbTableIndex.size() - 1;
    }

    private void updateDefaultFields(Object[] fieldsData) {
        for (FieldValue fieldValue : defaultValues) {
            int fieldIndex = getFieldIndex(fieldValue.getField());
            fieldsData[fieldIndex] = fieldValue.getValue().getData();
        }
    }

    public void commitUpdate(Object[] fieldsData) {
        dbTable.commitUpdate(fieldsData);

    }

    public void commitDelete(Object[] fieldsData) {
        dbTable.commitDelete(fieldsData);
    }

    public int getFieldIndex(Field field) {
        return dbTable.getFieldIndex(field);
    }

    public Row getOriginalRow(int index) {
        return dbTable.getOriginalRow(index2dbTableIndex.get(index));
    }

    public void closeRow(Row row) {
        rows.remove(row);
    }
}
