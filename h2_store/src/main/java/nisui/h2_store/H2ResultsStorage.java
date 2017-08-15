package nisui.h2_store;


import java.io.PrintStream;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Stream;

import nisui.core.ExperimentValuesHandler;
import nisui.core.ResultsStorage;

public class H2ResultsStorage<D, R> extends ResultsStorage<D, R> {
    private String filename;
    ExperimentValuesHandler<D> dataPointHandler;
    ExperimentValuesHandler<R> experimentResultHandler;

    public H2ResultsStorage(String filename, ExperimentValuesHandler<D> dataPointHandler, ExperimentValuesHandler<R> experimentResultHandler) {
        this.filename = filename;
        this.dataPointHandler = dataPointHandler;
        this.experimentResultHandler = experimentResultHandler;
    }

    @Override
    public void prepareStorage() {
        try (Connection con = connect()) {
            con.prepareTable(con.DATA_POINTS_TABLE_NAME, dataPointHandler);
            con.prepareTable(con.EXPERIMENT_RESULTS_TABLE_NAME, experimentResultHandler,
                    new H2FieldDefinition("data_point_id", long.class),
                    new H2FieldDefinition("seed", long.class));
        }
    }

    @Override
    public Connection connect() {
        return new Connection(filename);
    }

    public class Connection extends ResultsStorage<D, R>.Connection {
        private java.sql.Connection con;

        final static String DATA_POINTS_TABLE_NAME = "data_points";
        final static String EXPERIMENT_RESULTS_TABLE_NAME = "experiment_results";

        H2ResultsStorage<D, R> parent() {
            return H2ResultsStorage.this;
        }

        public Connection(String filename) {
            try {
                Class.forName("org.h2.Driver");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            try {
                con = DriverManager.getConnection("jdbc:h2:" + filename, "", "");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        PreparedStatement createPreparedStatement(String sql, Object... args) {
            try {
                return con.prepareStatement(String.format(sql, args));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void close() {
            try {
                con.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        public void prepareTable(String tableName, ExperimentValuesHandler<?> valueHandler, H2FieldDefinition... metaColumns) {
            Iterable<H2FieldDefinition> fields = () -> Stream.concat(
                    Arrays.stream(metaColumns),
                    valueHandler.fields().stream().map(H2FieldDefinition::new)
                    ).iterator();
            HashMap<String, H2FieldDefinition> existingFields = getTableColumns(tableName);
            if (existingFields == null) {
                buildTable(tableName, fields);
            } else {
                fixTable(tableName, existingFields, fields);
            }
        }

        private void buildTable(String tableName, Iterable<H2FieldDefinition> fields) {
            try (Statement stmt = con.createStatement()) {
                StringBuilder sql = new StringBuilder();
                sql.append("CREATE TABLE ").append(tableName).append("(id IDENTITY PRIMARY KEY");
                for (H2FieldDefinition field : fields) {
                    sql.append(", ").append(field.getName()).append(" ").append(field.getSqlType());
                }
                sql.append(");");
                stmt.execute(sql.toString());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        private void fixTable(String tableName, HashMap<String, H2FieldDefinition> existingFields, Iterable<H2FieldDefinition> fields) {
            try (Statement stmt = con.createStatement()) {
                for (H2FieldDefinition field : fields) {
                    H2FieldDefinition existingField = existingFields.remove(field.getName().toLowerCase());
                    if (existingField == null) {
                        field.addToTable(stmt, tableName);
                    } else if (!field.equals(existingField)) {
                        field.fixInTable(stmt, tableName);
                    }
                }
                for (H2FieldDefinition remainingField : existingFields.values()) {
                    remainingField.removeFromTable(stmt, tableName);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        private HashMap<String, H2FieldDefinition> getTableColumns(String tableName) {
            try (Statement stmt = con.createStatement()) {
                ResultSet rs = stmt.executeQuery(String.format("SHOW COLUMNS FROM %s;", tableName));
                if (!rs.next()) {
                    // no table
                    return null;
                }
                HashMap<String, H2FieldDefinition> fields = new HashMap<>();
                while (rs.next()) {
                    H2FieldDefinition field = H2FieldDefinition.fromResultSet(rs);
                    fields.put(field.getName().toLowerCase(), field);
                }
                return fields;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        public void print(PrintStream outStream, String sql, Object... args) {
            try (Statement stmt = con.createStatement()) {
                ResultSet rs = stmt.executeQuery(String.format(sql, args));
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); ++i) {
                    outStream.printf("%s\t", rs.getMetaData().getColumnName(i));
                }
                outStream.println();
                outStream.println("-------------------------------");
                while (rs.next()) {
                    for (int i = 1; i <= rs.getMetaData().getColumnCount(); ++i) {
                        outStream.printf("%s\t", rs.getObject(i));
                    }
                    outStream.println();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public H2Operations.InsertDataPoint<D, R> insertDataPoints() {
            return new H2Operations.InsertDataPoint<>(this);
        }
    }
}
