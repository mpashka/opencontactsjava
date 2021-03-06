package org.mpn.contacts.framework.db;

import org.apache.log4j.Logger;
import org.mpn.contacts.framework.FatalException;
import org.mpn.contacts.framework.ContactsException;

import java.sql.*;
import java.util.Map;
import java.util.HashMap;

/**
 * @author <a href="mailto:pmoukhataev@dev.java.net">Pavel Moukhataev</a>
 * @version $Id$
 */
public class DbAccess {

    final static Logger log = Logger.getLogger(DbAccess.class);

    private static DbAccess dbAccess;
    private static Map<Class, String> sqlTypesMap = new HashMap<Class, String>();
    static {
        sqlTypesMap.put(Integer.class,          "INTEGER");
        sqlTypesMap.put(Double.class,           "DOUBLE");
        sqlTypesMap.put(String.class,           "VARCHAR");
        sqlTypesMap.put(java.util.Date.class,     "TIMESTAMP ");
        sqlTypesMap.put(java.sql.Date.class,      "DATE ");
        sqlTypesMap.put(java.sql.Time.class,      "TIME ");
        sqlTypesMap.put(java.sql.Timestamp.class, "TIMESTAMP ");
        sqlTypesMap.put(Boolean.class,          "BOOLEAN");
        sqlTypesMap.put(Long.class,             "BIGINT");
        sqlTypesMap.put(byte[].class,           "BINARY");
    }

    private String driverClassName;
    private String driverUrl;
    private String driverLogin;
    private String driverPassword;

    private Connection connection;

    private Map<String, DbTable> tables = new HashMap<String, DbTable>();

    private DbAccess() {
        driverClassName = "org.hsqldb.jdbcDriver";
// File database   "jdbc:hsqldb:file:/opt/db/testdb"
// Server database "jdbc:hsqldb:hsql://localhost/xdb"
// Memory database "jdbc:hsqldb:mem:aname"
        driverUrl = "jdbc:hsqldb:file:target/testdb";
        driverLogin = "sa";
        driverPassword = "";

        try {
            Class.forName(driverClassName);
            connection = DriverManager.getConnection(driverUrl, driverLogin, driverPassword);
//            connection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
            connection.setAutoCommit(true);
            loadExistingTables();
        } catch (Exception e) {
            log.error("Error initializing driver", e);
            throw new FatalException(e);
        }
    }

    public static DbAccess getInstance() {
        if (dbAccess == null) {
            dbAccess = new DbAccess();
        }
        return dbAccess;
    }

    public void commit() throws SQLException {
        connection.commit();
    }

    public void close() throws SQLException {
        for (DbTable dbTable : tables.values()) {
            if (dbTable != null) {
                dbTable.close();
            }
        }
        connection.close();
        log.info("Database closed");
    }

    private void loadExistingTables() {
        try {
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            ResultSet catalogsResultSet = databaseMetaData.getCatalogs();
            while (catalogsResultSet.next()) {
                log.debug(" Catalog = " + catalogsResultSet.getString("TABLE_CAT"));
            }

            ResultSet tablesResultSet = databaseMetaData.getTables(null, null, null, null);
            while (tablesResultSet.next()) {
//                log.debug(" TABLE_CAT:" + tablesResultSet.getString("TABLE_CAT"));
//                log.debug(" TABLE_SCHEM:" + tablesResultSet.getString("TABLE_SCHEM"));
//                log.debug(" TABLE_NAME:" + tablesResultSet.getString("TABLE_NAME"));
//                log.debug(" TABLE_TYPE:" + tablesResultSet.getString("TABLE_TYPE"));
//                log.debug(" REMARKS:" + tablesResultSet.getString("REMARKS"));
//                log.debug(" TYPE_CAT:" + tablesResultSet.getString("TYPE_CAT"));
//                log.debug(" TYPE_SCHEM:" + tablesResultSet.getString("TYPE_SCHEM"));
//                log.debug(" TYPE_NAME:" + tablesResultSet.getString("TYPE_NAME"));
//                log.debug(" SELF_REFERENCING_COL_NAME:" + tablesResultSet.getString("SELF_REFERENCING_COL_NAME"));
//                log.debug(" REF_GENERATION:" + tablesResultSet.getString("REF_GENERATION"));
                String tableName = tablesResultSet.getString("TABLE_NAME").toLowerCase();
                tables.put(tableName, null);
            }

        } catch (SQLException e) {
            log.error("Error listing tables");
        }
    }

    public void createTable(DbTable dbTable) {
        String tableName = dbTable.getName().toLowerCase();
        if (tables.containsKey(tableName)) {
            tables.put(tableName, dbTable);
            return;
        }
        StringBuilder sqlStatement = new StringBuilder();
        sqlStatement.append("CREATE TABLE ");
        sqlStatement.append(dbTable.getName());
        sqlStatement.append(" \n(\n");
        Field idField = dbTable.getFieldsMetaData()[0];
        sqlStatement.append(idField.getName());
        sqlStatement.append(" ");
        sqlStatement.append(sqlTypesMap.get(idField.getTypeClass()));
        sqlStatement.append(" GENERATED BY DEFAULT AS IDENTITY NOT NULL ");

        for (int i = 1; i < dbTable.getFieldsMetaData().length; i++) {
            Field field = dbTable.getFieldsMetaData()[i];
            sqlStatement.append(", \n");
            sqlStatement.append(field.getName());
            sqlStatement.append(" ");
            String sqlDataType = sqlTypesMap.get(field.getTypeClass());
            if (sqlDataType == null) {
                throw new FatalException("Unknown data type : " + field.getTypeClass());
            }
            sqlStatement.append(sqlDataType);
            sqlStatement.append(" ");
        }
        sqlStatement.append(");");

        try {
            log.info("Creating DB table. SQL:" + sqlStatement);
            connection.createStatement().execute(
                    sqlStatement.toString()
            );
        } catch (SQLException e) {
            log.error("Error creating table", e);
//            throw new ContactsException(e);
        }
    }

    public PreparedStatement getTableSelectStatement(DbTable dbTable) {
        try {
            PreparedStatement selectStatement = connection.prepareStatement("SELECT * FROM " + dbTable.getName()
                    , ResultSet.TYPE_SCROLL_SENSITIVE
                    , ResultSet.CONCUR_UPDATABLE);
            return selectStatement;
        } catch (SQLException e) {
            log.error("Error prepare table select statement", e);
            throw new ContactsException(e);
        }
    }

    public PreparedStatement getTableInsertStatement(DbTable dbTable) {
        try {
            String idFieldName = dbTable.getFieldsMetaData()[0].getName();
            StringBuilder columnsString = new StringBuilder(" (" + idFieldName);
            StringBuilder valuesString = new StringBuilder(") VALUES (NULL");

            for (int i = 1; i < dbTable.getFieldsMetaData().length; i++) {
                columnsString.append(", ");
                valuesString.append(", ");
                columnsString.append(dbTable.getFieldsMetaData()[i].getName());
                valuesString.append("?");
            }
            PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO " + dbTable.getName() + columnsString + valuesString + ");");
            return insertStatement;
        } catch (SQLException e) {
            log.error("Error prepare table select statement", e);
            throw new ContactsException(e);
        }
    }

    public PreparedStatement getTableInsertIdStatement() {
        try {
            PreparedStatement insertIdStatement = connection.prepareStatement("CALL IDENTITY();");
            return insertIdStatement;
        } catch (SQLException e) {
            log.error("Error prepare table select statement", e);
            throw new ContactsException(e);
        }
    }

    public PreparedStatement getTableUpdateStatement(DbTable dbTable) {
        try {
            String idFieldName = dbTable.getFieldsMetaData()[0].getName();
            StringBuilder updateStatementString = new StringBuilder("UPDATE " + dbTable.getName() + " SET ");
            for (int i = 1; i < dbTable.getFieldsMetaData().length; i++) {
                if (i > 1) {
                    updateStatementString.append(", \n");
                }
                updateStatementString.append(dbTable.getFieldsMetaData()[i].getName());
                updateStatementString.append(" = ?");
            }
            PreparedStatement updateStatement = connection.prepareStatement(updateStatementString + " WHERE "
                    + idFieldName + " = ?;");
            return updateStatement;
        } catch (SQLException e) {
            log.error("Error prepare table select statement", e);
            throw new ContactsException(e);
        }
    }

    public PreparedStatement getSelectStatement(String sqlStatement) {
        try {
            PreparedStatement selectStatement = connection.prepareStatement(sqlStatement
                    , ResultSet.TYPE_SCROLL_SENSITIVE
                    , ResultSet.CONCUR_UPDATABLE);
            return selectStatement;
        } catch (SQLException e) {
            log.error("Error prepare statement", e);
            throw new ContactsException(e);
        }
    }


}
