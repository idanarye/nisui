package nisui.h2_store;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import nisui.core.ExperimentValuesHandler;

public class H2FieldDefinition {
    public static String typeToSqlName(Class<?> type) {
        if (type.isAssignableFrom(long.class)) {
            return "BIGINT";
        }
        if (type.isAssignableFrom(int.class)) {
            return "INTEGER";
        }
        if (type.isAssignableFrom(short.class)) {
            return "SMALLINT";
        }
        if (type.isAssignableFrom(byte.class)) {
            return "TINYINT";
        }
        if (type.isAssignableFrom(boolean.class)) {
            return "BOOLEAN";
        }
        if (type.isAssignableFrom(double.class)) {
            return "DOUBLE";
        }
        if (type.isAssignableFrom(float.class)) {
            return "REAL";
        }
        if (type.isAssignableFrom(String.class)) {
            return "VARCHAR";
        }
        if (type.isEnum()) {
            return "VARCHAR";
        }
        if (type.isArray()) {
            return "ARRAY";
        }
        return null;
    }

    public static Class<?> sqlNameToType(String sqlName) {
        int paren = sqlName.indexOf("(");
        if (-1 < paren) {
            sqlName = sqlName.substring(0, paren);
        }
        switch (sqlName.toUpperCase()) {
            case "BIGINT":
                return long.class;
            case "INTEGER":
                return int.class;
            case "SMALLINT":
                return short.class;
            case "TINYINT":
                return byte.class;
            case "BOOLEAN":
                return boolean.class;
            case "DOUBLE":
                return double.class;
            case "REAL":
                return float.class;
            case "VARCHAR":
                return String.class;
            case "ARRAY":
                return Object[].class;
            default:
                return null;
        }
    }

    private String name;
    private Class<?> type;

    public H2FieldDefinition(String name, Class type) {
        this.name = name;
        this.type = type;
    }

    public H2FieldDefinition(ExperimentValuesHandler<?>.Field field) {
        this(field.getName(), field.getType());
    }

    static H2FieldDefinition fromResultSet(ResultSet rs) throws SQLException {
        return new H2FieldDefinition(rs.getString("COLUMN_NAME"), sqlNameToType(rs.getString("TYPE")));
    }

    public String getName() {
        return name;
    }

    public Class getType() {
        return type;
    }

    public String getSqlType() {
        return typeToSqlName(type);
    }

    @Override
    public String toString() {
        return String.format("%s(%s)", name, type);
    }

    @Override
    public boolean equals(Object other_) {
        if (other_ instanceof H2FieldDefinition) {
            H2FieldDefinition other = (H2FieldDefinition)other_;
            return this.name.equalsIgnoreCase(other.name)
                && this.type.equals(other.type);
        } else {
            return false;
        }
    }

    void addToTable(Statement stmt, String tableName) throws SQLException {
        stmt.execute(String.format("ALTER TABLE %s ADD COLUMN %s %s;", tableName, name, getSqlType()));
    }

    void removeFromTable(Statement stmt, String tableName) throws SQLException {
        stmt.execute(String.format("ALTER TABLE %s DROP COLUMN %s;", tableName, name));
    }

    void fixInTable(Statement stmt, String tableName) throws SQLException {
        stmt.execute(String.format("ALTER TABLE %s ALTER COLUMN %s %s;", tableName, name, getSqlType()));
    }
}
