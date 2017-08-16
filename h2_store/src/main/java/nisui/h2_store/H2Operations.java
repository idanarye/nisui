package nisui.h2_store;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

import nisui.core.*;

public abstract class H2Operations<D, R> implements AutoCloseable {
    protected H2ResultsStorage<D, R>.Connection con;
    protected PreparedStatement stmt;

    public H2Operations(H2ResultsStorage<D, R>.Connection con) {
        this.con = con;
    }

    @Override
    public void close() throws SQLException {
        if (stmt != null) {
            stmt.close();
        }
    }

    public static class InsertDataPoint<D, R> extends H2Operations<D, R> implements DataPointInserter<D> {
        InsertDataPoint(H2ResultsStorage<D, R>.Connection con) {
            super(con);
            StringBuilder sql = new StringBuilder();
            sql.append("INSERT INTO ").append(con.DATA_POINTS_TABLE_NAME).append("(");
            boolean wroteFirst = false;
            for (ExperimentValuesHandler<D>.Field field : con.parent().dataPointHandler.fields()) {
                if (wroteFirst) {
                    sql.append(", ");
                } else {
                    wroteFirst = true;
                }
                sql.append(field.getName());
            }
            sql.append(") VALUES(");
            int numberOfFields = con.parent().dataPointHandler.fields().size();
            for (int i = 0; i < numberOfFields; ++i) {
                if (0 < i) {
                    sql.append(", ");
                }
                sql.append("?");
            }
            sql.append(");");
            stmt = con.createPreparedStatement(sql.toString());
        }

        public void insert(D dataPoint) throws SQLException {
            stmt.clearParameters();
            int paramIndex = 1;
            for (ExperimentValuesHandler<D>.Field field : con.parent().dataPointHandler.fields()) {
                stmt.setObject(paramIndex, field.get(dataPoint));
                ++paramIndex;
            }
            stmt.executeUpdate();
        }
    }

    public static class ReadDataPoints<D, R> extends H2Operations<D, R> implements DataPointsReader<D> {
        ReadDataPoints(H2ResultsStorage<D, R>.Connection con) {
            super(con);
        }

        @Override
        public RSIterator iterator() {
            if (stmt == null) {
                StringBuilder sql = new StringBuilder();
                sql.append("SELECT id");
                for (ExperimentValuesHandler<D>.Field field : con.parent().dataPointHandler.fields()) {
                    sql.append(", ").append(field.getName());
                }
                sql.append(" FROM ").append(con.DATA_POINTS_TABLE_NAME).append(';');
                stmt = con.createPreparedStatement(sql.toString());
            }
            try {
                return new RSIterator(stmt.executeQuery());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        private class RSIterator implements Iterator<DataPoint<D>> {
            private ResultSet rs;
            private H2DataPoint<D> next;

            public RSIterator(ResultSet rs) {
                this.rs = rs;
                next = null;
                next();
            }

            @Override
            public boolean hasNext() {
                return next != null;
            }

            @Override
            public H2DataPoint<D> next() {
                try {
                    H2DataPoint<D> currentNext = next;
                    if (rs.next()) {
                        D value = con.parent().dataPointHandler.createValue();
                        int i = 2;
                        for (ExperimentValuesHandler<D>.Field field : con.parent().dataPointHandler.fields()) {
                            field.set(value, rs.getObject(i));
                            ++i;
                        }
                        next = new H2DataPoint<>(value, rs.getLong(1));
                    } else {
                        next = null;
                    }
                    return currentNext;
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
