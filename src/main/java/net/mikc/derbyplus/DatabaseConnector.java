package net.mikc.derbyplus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import net.mikc.derbyplus.commands.DatabaseCommand;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseConnector {
    private static final String DERBY_DRIVER_CLASS = "org.apache.derby.jdbc.EmbeddedDriver";
    private static final String H2_DRIVER_CLASS = "org.h2.Driver";

    private Connection connection;
    private String jdbcUrl;
    private final IConsoleLogger console;

    public DatabaseConnector(IConsoleLogger console) {
        this.console = console;
    }

    public void execute(DatabaseCommand cmd, String... args) {
        if (!checkConnection(cmd)) return;

        try {
            switch (cmd) {
                case CONNECT:
                    connect(args[0]);
                    break;
                case EXIT:
                    exit();
                    break;
                case SHOW_TABLES:
                    showTables();
                    break;
                case GET_SCHEMA:
                    getSchema();
                    break;
                case SELECT:
                    select(args[0]);
                    break;
                case CREATE_TABLE:
                    createTable(args[0]);
                    break;
                case UPDATE_INSERT:
                    updateOrInsert(args[0]);
                    break;
                case DESC_TABLE:
                    descTable(args[0]);
                    break;
                default:
                    console.log("Unknown command: " + cmd);

            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void descTable(String table) throws SQLException {
        final Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("select * from "+table);
        List<QueryColumnResultSchema> schema = fetchSchema(rs);
        console.log("Table: "+table);
        for(QueryColumnResultSchema column: schema) {
            console.log("   "+column.getColumnName() + " " + column.getColumnType()+"("+column.getColumnPrecision()+")");
        }
    }

    private void connect(String jdbcUrl) throws ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException {
        Class.forName(getDriver(jdbcUrl)).newInstance();
        this.connection = DriverManager.getConnection(jdbcUrl);
        this.jdbcUrl = jdbcUrl;
        console.log("Connected to: " + jdbcUrl);
    }

    private void exit() throws SQLException {
        console.log("Closing connection to :" + jdbcUrl);
        connection.close();
    }

    private void getSchema() throws SQLException {
        String schema = connection.getSchema();
        console.log("Schema: " + schema);
    }

    private void createTable(String sqlStatement) throws SQLException {
        final Statement stmt = connection.createStatement();
        stmt.execute(sqlStatement);
        console.log("Invoke `show tables` if the table is created");
    }

    private void updateOrInsert(String sqlStatement) throws SQLException {
        final Statement stmt = connection.createStatement();
        int rows = stmt.executeUpdate(sqlStatement);
        console.log("Affected " + rows + " row(s).");
    }

    private void select(String sqlStatement) throws SQLException {
        final Statement stmt = connection.createStatement();
        console.log("Executing: |" + sqlStatement + "|");
        final java.sql.ResultSet rs = stmt.executeQuery(sqlStatement);
        List<QueryColumnResultSchema> schema = fetchSchema(rs);
        while (rs.next()) {
            List<String> row = new ArrayList<>();
            for (QueryColumnResultSchema queryColumnResultSchema : schema) {
                String columnName = queryColumnResultSchema.getColumnName();
                String columnType = queryColumnResultSchema.getColumnType();
                String value = getValue(rs, columnName, columnType);
                row.add(value);
            }
            console.log(row.toString());
        }
        rs.close();
        stmt.close();
    }

    private void showTables() throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        String[] types = {"TABLE"};
        //Retrieving the columns in the database
        ResultSet tables = metaData.getTables(null, null, "%", types);
        while (tables.next()) {
            console.log(tables.getString("TABLE_NAME"));
        }
    }

    private static String getValue(ResultSet rs, String columnName, String columnType) throws SQLException {
        switch (columnType) {
            case "INTEGER":
                return String.valueOf(rs.getInt(columnName));
            case "BIGINT":
                return String.valueOf(rs.getLong(columnName));
            case "DOUBLE":
                return String.valueOf(rs.getDouble(columnName));
            case "VARCHAR":
                return rs.getString(columnName);
            case "CLOB":
                return convertClobToString(rs.getClob(columnName));
            case "BLOB":
                return convertBlobToString(rs.getBlob(columnName));
            default:
                throw new IllegalArgumentException("Can't handle: " + columnType);

        }
    }

    private static String convertBlobToString(Blob blob) {
        return blob == null ? null : blob.toString();
    }

    private static String convertClobToString(Clob clob) {
//        StringBuffer buffer = new StringBuffer();
//        int ch;
//        while ((ch = clob.getCharacterStream().read())!=-1) {
//            buffer.append(""+(char)ch);
//        }
//        return buffer.toString();
        return clob == null ? null : clob.toString();
    }

    private List<QueryColumnResultSchema> fetchSchema(ResultSet rs) throws SQLException {
        final List<QueryColumnResultSchema> schema = new ArrayList<>();
        final ResultSetMetaData metaData = rs.getMetaData();
        final int columnCount = metaData.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            String columnName = metaData.getColumnName(i);
            //FIXME Question we can fetch type numer - is it better?
            String columnType = metaData.getColumnTypeName(i);
            boolean fetchScale = true;
            boolean fetchPrecision = true;
            Integer precision = fetchPrecision ? metaData.getPrecision(i) : null;
            Integer scale = fetchScale ? metaData.getScale(i) : null;
            schema.add(
                    QueryColumnResultSchema.builder()
                            .columnName(columnName)
                            .columnType(columnType)
                            .columnPrecision(BigDecimal.valueOf(precision))
                            .columnScale(BigDecimal.valueOf(scale))
                            .build()
            );
        }
        return schema;
    }

    private boolean checkConnection(DatabaseCommand cmd) {
        if (DatabaseCommand.CONNECT.equals(cmd)) return true;
        if (connection == null) {
            console.log("Not logged in, you must use `connect jdbcUrl`");
            return false;
        }
        return true;
    }

    private static String getDriver(String jdbcUri) {
        if (jdbcUri.startsWith("jdbc:derby")) {
            return DERBY_DRIVER_CLASS;
        }
        if (jdbcUri.startsWith("jdbc:h2")) {
            return H2_DRIVER_CLASS;
        }
        throw new IllegalArgumentException("can't find driver for: " + jdbcUri);
    }

    @Builder
    @AllArgsConstructor
    @Getter
    static class QueryColumnResultSchema {
        private String columnName;
        private String columnType;
        private BigDecimal columnPrecision;
        private BigDecimal columnScale;
    }
}
