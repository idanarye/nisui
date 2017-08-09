package nisui.h2_store;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import nisui.core.ExperimentValuesHandler;
import nisui.core.ResultsStorage;

public class H2Connection extends ResultsStorage.Connection {
    private Connection con;

    public H2Connection(String filename) {
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

    PreparedStatement statement(String sql) {
        try {
            return con.prepareStatement(sql);
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

    private String typeToSqlName(Class<?> clazz) {
        if (clazz.isAssignableFrom(long.class)) {
            return "BIGINT";
        }
        if (clazz.isAssignableFrom(int.class)) {
            return "INT";
        }
        if (clazz.isAssignableFrom(short.class)) {
            return "SMALLINT";
        }
        if (clazz.isAssignableFrom(byte.class)) {
            return "TINYINT";
        }
        if (clazz.isAssignableFrom(boolean.class)) {
            return "BOOLEAN";
        }
        if (clazz.isAssignableFrom(double.class)) {
            return "DOUBLE";
        }
        if (clazz.isAssignableFrom(float.class)) {
            return "REAL";
        }
        return null;
    }

    public void prepareTable(String tableName, ExperimentValuesHandler<?> valueHandler) {
        try (Statement stmt = con.createStatement()) {
            StringBuilder sql = new StringBuilder();
            sql.append("CREATE TABLE ").append(tableName).append("(id IDENTITY PRIMARY KEY");
            for (ExperimentValuesHandler<?>.Field field : valueHandler.fields()) {
                sql.append(", ").append(field.getName()).append(" ").append(typeToSqlName(field.getType()));
            }
            sql.append(");");
            stmt.execute(sql.toString());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void print(PrintStream outStream, String sql) {
        try (Statement stmt = con.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            for (int i = 1; i <= rs.getMetaData().getColumnCount(); ++i) {
                outStream.printf("%s\t", rs.getMetaData().getColumnName(i));
            }
            outStream.println();
            outStream.println("-------------------------------");
            while(rs.next()) {
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); ++i) {
                    outStream.printf("%s\t", rs.getObject(i));
                }
                outStream.println();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
