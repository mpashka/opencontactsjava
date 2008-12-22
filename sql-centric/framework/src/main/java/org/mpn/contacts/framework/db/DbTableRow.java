package org.mpn.contacts.framework.db;

import org.apache.log4j.Logger;
import org.mpn.contacts.framework.EventGeneratorBase;

import java.util.Arrays;

/**
 * @author <a href="mailto:pmoukhataev@dev.java.net">Pavel Moukhataev</a>
 * @version $Id$
 */
public class DbTableRow extends EventGeneratorBase<Row> implements Row {

    final static Logger log = Logger.getLogger(DbTableRow.class);


    private final DataSource dbTable;
    private int index;
    private final Object[] rowData;


    public DbTableRow(DataSource dbTable, Field[] fields) {
        this(dbTable, fields, 0);
    }

    public DbTableRow(DataSource dbTable, Field[] fields, int index) {
        this.dbTable = dbTable;
        rowData = new Object[fields.length];
        this.index = index;
        update();
    }

    public DataSource getDataSource() {
        return dbTable;
    }

    public void startIteration() {
        index = -1;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public boolean hasData() {
        return dbTable.getRowCount() > 0;
    }

    public boolean hasNext() {
        return hasData() && index + 1 < dbTable.getRowCount();
    }

    public boolean hasPrevious() {
        return hasData() && index > 0;
    }

    public void first() {
        index = 0;
        update();
    }

    public void last() {
        index = dbTable.getRowCount() - 1;
        update();
    }

    public Row next() {
        index++;
        update();
        return this;
    }

    public void previous() {
        index--;
        update();
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean update() {
        boolean indexInBounds = false;
        int rowCount = dbTable.getRowCount();
        if (rowCount == 0) {
            Arrays.fill(rowData, null);
        } else {
            if (index < 0) {
                index = 0;
            } else if (index >= rowCount) {
                index = rowCount - 1;
            } else {
                indexInBounds = true;
            }
            System.arraycopy(dbTable.getRowData(index), 0, rowData, 0, rowData.length);
        }
        notifyListeners(this);
        return indexInBounds;
    }

    public void clearData() {
        Arrays.fill(rowData, null);
    }

    public void commitInsert() {
        index = dbTable.commitInsert(rowData);
    }

    public void commitUpdate() {
        dbTable.commitUpdate(rowData);
    }

    public void commitDelete() {
        dbTable.commitDelete(rowData);
    }

    public Object[] getData() {
        return rowData;
    }

    public Long getId() {
        return (Long) rowData[0];
    }

    public <Type> Type getData(Field<Type> field) {
        //noinspection unchecked
        return (Type) rowData[dbTable.getFieldIndex(field)];
    }

    public <Type> void setData(Field<Type> field, Type data) {
        rowData[dbTable.getFieldIndex(field)] = data;
    }

    public Row getOriginalRow() {
        return dbTable.getOriginalRow(index);
    }

    public void close() {
        dbTable.closeRow(this);
    }
}
