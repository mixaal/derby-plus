package net.mikc.derbyplus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.io.IOException;
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

    public void connect(String jdbcUrl) {
        try {
            Class.forName(getDriver(jdbcUrl)).newInstance();
            this.connection = DriverManager.getConnection(jdbcUrl);
            this.jdbcUrl = jdbcUrl;

            console.log("Connected to: "+jdbcUrl);
        }
        catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void exit() {
        try {
            console.log("Closing connection to :"+jdbcUrl);
            connection.close();
        }
        catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void getSchema() {
        try {
            String schema = connection.getSchema();
            console.log("Schema: " + schema);
        }
        catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void sqlCommand(String sqlStatement) {
        try {
            final Statement stmt = connection.createStatement();
            console.log("Executing: |"+sqlStatement+"|");
            final java.sql.ResultSet rs = stmt.executeQuery(sqlStatement);
            List<QueryColumnResultSchema> schema = fetchSchema(rs);
            while (rs.next()) {
                List<String> row = new ArrayList<>();
                for (int i = 0; i < schema.size(); i++) {
                    String columnName = schema.get(i).getColumnName();
                    String columnType = schema.get(i).getColumnType();
                    String value = getValue(rs, columnName, columnType);
                    row.add(value);
                }
                console.log(row.toString());
            }
            rs.close();
            stmt.close();

        }
        catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void showTables() {
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            String[] types = {"TABLE"};
            //Retrieving the columns in the database
            ResultSet tables = metaData.getTables(null, null, "%", types);
            while (tables.next()) {
                console.log(tables.getString("TABLE_NAME"));
            }
        }
        catch(Throwable t) {
            t.printStackTrace();
        }
    }

    private static String getValue(ResultSet rs, String columnName, String columnType) throws SQLException, IOException {
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

    private static String convertBlobToString(Blob blob) throws SQLException, IOException {
        return blob == null ? null : blob.toString();
    }
    private static String convertClobToString(Clob clob) throws SQLException, IOException {
        StringBuffer buffer = new StringBuffer();
//        int ch;
//        while ((ch = clob.getCharacterStream().read())!=-1) {
//            buffer.append(""+(char)ch);
//        }
//        return buffer.toString();
        return  clob==null ? null : clob.toString();
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
