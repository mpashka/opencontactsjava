package org.mpn.contacts.framework.db;

import org.apache.log4j.Logger;
import org.mpn.contacts.framework.ContactsException;
import org.mpn.contacts.framework.EventGeneratorBase;
import org.mpn.contacts.framework.Value;
import org.mpn.contacts.framework.db.filter.FixedValueFilter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * @author <a href="mailto:pmoukhataev@dev.java.net">Pavel Moukhataev</a>
 * @version $Id$
 */
public class DbTable extends EventGeneratorBase<DataSource> implements DataSource {

    final static Logger log = Logger.getLogger(DbTable.class);

    private static final DbAccess dbAccess = DbAccess.getInstance();

    private static final Iterator<Row> EMPTY_ITERATOR = new Iterator<Row>() {
        public boolean hasNext() {
            return false;
        }

        public Row next() {
            throw new NoSuchElementException();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    };

    private String name;
    public Field<Long> id;
    private Field[] fieldsMetaData;
    private final List<Object[]> tableData = new ArrayList<Object[]>();

    private PreparedStatement selectDataPreparedStatement;
    private PreparedStatement insertDataPreparedStatement;
    private PreparedStatement insertIdDataPreparedStatement;
    private PreparedStatement updateDataPreparedStatement;

    private final Map<Field, Integer> fieldIndexes = new HashMap<Field, Integer>();

    private Set<DataSourceListener> listeners = new HashSet<DataSourceListener>();

    public DbTable(String name, Field... fields) {
        this.name = name;
        this.id = new Field<Long>(Long.class, name + "Id");
        this.fieldsMetaData = new Field[fields.length + 1];
        this.fieldsMetaData[0] = id;
        System.arraycopy(fields, 0, this.fieldsMetaData, 1, fields.length);

        dbAccess.createTable(this);
        selectDataPreparedStatement = dbAccess.getTableSelectStatement(this);
        insertDataPreparedStatement = dbAccess.getTableInsertStatement(this);
        insertIdDataPreparedStatement = dbAccess.getTableInsertIdStatement();
        updateDataPreparedStatement = dbAccess.getTableUpdateStatement(this);
        for (int i = 0; i < this.fieldsMetaData.length; i++) {
            fieldIndexes.put(this.fieldsMetaData[i], i);
        }
        readTableData();
    }

    public void close() throws SQLException {
        selectDataPreparedStatement.close();
        insertDataPreparedStatement.close();
        insertIdDataPreparedStatement.close();
        updateDataPreparedStatement.close();
    }

    public Field<Long> getIdField() {
        return id;
    }

    private void readTableData() {
        try {
            ResultSet resultSet = selectDataPreparedStatement.executeQuery();
            while (resultSet.next()) {
                Object[] fieldsData = new Object[fieldsMetaData.length];
                for (int i = 0; i < fieldsMetaData.length; i++) {
                    fieldsData[i] = resultSet.getObject(fieldsMetaData[i].getName());
                }
                tableData.add(fieldsData);
            }
            resultSet.close();
        } catch (SQLException e) {
            log.error("Error creating table", e);
            throw new ContactsException(e);
        }
    }

    public <Type> DataSource getFilteredTable(Field<Type> field, Value<Type> value) {
        FixedValueFilter<Type> filter = new FixedValueFilter<Type>(field, value);
        return new FilteredDataSource(this, filter, new FieldValue<Type>(field, value));
    }

    public String getName() {
        return name;
    }

    public Field[] getFieldsMetaData() {
        return fieldsMetaData;
    }

    public List<Object[]> getTableData() {
        return tableData;
    }

    public Object[] getRowData(int index) {
        return tableData.get(index);
    }

    public int getRowCount() {
        return tableData.size();
    }

    public int getFieldIndex(Field field) {
        Integer index = fieldIndexes.get(field);
        if (index == null) {
            log.error("Field " + field + " not found in table " + this);
        }
        return index;
    }


    public Row getRow() {
        return new DbTableRow(this, fieldsMetaData);
    }

    public Iterator<Row> iterator() {
        if (getRowCount() == 0) return EMPTY_ITERATOR;
        Row row = getRow();
        row.startIteration();
        return row;
    }

    public int commitInsert(Object[] fieldsData) {
        // todo [!] refresh row
        try {
            log.debug("Insert data into table " + name + " : " + Arrays.toString(fieldsData));
            for (int i = 1; i < fieldsData.length; i++) {
                setSqlStatementObject(insertDataPreparedStatement, fieldsMetaData[i], fieldsData[i], i);
            }
            insertDataPreparedStatement.executeUpdate();
            ResultSet insertIdData = insertIdDataPreparedStatement.executeQuery();
            insertIdData.next();
            fieldsData[0] = insertIdData.getLong(1);
            insertIdData.close();
            dbAccess.commit();

            Object[] newFieldData = new Object[fieldsData.length];
            System.arraycopy(fieldsData, 0, newFieldData, 0, fieldsData.length);
            tableData.add(newFieldData);
            int rowIndex = tableData.size() - 1;
            for (DataSourceListener dataSourceListener : listeners) {
                dataSourceListener.onInsert(rowIndex, fieldsData);
            }
            return rowIndex;
        } catch (SQLException e) {
            log.error("Error description", e);
            throw new ContactsException("Error description", e);
        }
    }

    public void commitUpdate(Object[] fieldsData) {
        try {
            log.debug("Update data in table " + name + " : " + Arrays.toString(fieldsData));
            for (int i = 1; i < fieldsData.length; i++) {
                setSqlStatementObject(updateDataPreparedStatement, fieldsMetaData[i], fieldsData[i], i);
//                updateDataPreparedStatement.setObject(i, fieldsData[i]);
            }
            updateDataPreparedStatement.setLong(fieldsData.length, (Long) fieldsData[0]);
            int rowCount = updateDataPreparedStatement.executeUpdate();
            if (rowCount != 1) {
                log.error("Error executing update : " + rowCount);
            }
            dbAccess.commit();

            int rowIndex = findRowById((Long) fieldsData[0]);
            if (rowIndex == -1) {
                log.error("Can't find updatebale row for ID : " + fieldsData[0] + " in the table " + name);
                return;
            }
            Object[] newFieldData = tableData.get(rowIndex);
            System.arraycopy(fieldsData, 0, newFieldData, 0, fieldsData.length);
            for (DataSourceListener dataSourceListener : listeners) {
                dataSourceListener.onUpdate(rowIndex, fieldsData);
            }
        } catch (SQLException e) {
            log.error("Error description", e);
            throw new ContactsException("Error description", e);
        }
    }

    private void setSqlStatementObject(PreparedStatement sqlStatement, Field fieldMetadata, Object fieldData, int index) throws SQLException {
//        if (fieldData == null) return;
        if (fieldMetadata.getTypeClass() == Date.class) {
            log.debug("Iserting date : " + fieldData);
            if (fieldData == null) {
                sqlStatement.setDate(index, null);
                return;
            }
            Date date = (Date) fieldData;
            java.sql.Date sqlDate = new java.sql.Date(date.getTime());
            sqlStatement.setDate(index, sqlDate);
        } else {
            sqlStatement.setObject(index, fieldData);
        }
    }

    public void commitDelete(Object[] fieldsData) {
        // todo [!] unsupported
    }

    public void addListener(DataSourceListener listener) {
        listeners.add(listener);
    }

    public void removeListener(DataSourceListener listener) {
        listeners.remove(listener);
    }

    private int findRowById(Long id) {
        for (ListIterator<Object[]> iterator = tableData.listIterator(); iterator.hasNext();) {
            Object[] fields = iterator.next();
            if (fields[0].equals(id)) return iterator.previousIndex();
        }
        return -1;
    }

    public Row getOriginalRow(int index) {
        return new DbTableRow(this, fieldsMetaData, index);
    }

    public void closeRow(Row row) {
    }

    public String toString() {
        return "{DbTable@" + System.identityHashCode(this) + ": " + name + "}";
    }

}
