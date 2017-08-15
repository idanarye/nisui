package nisui.h2_store;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import nisui.core.*;

public abstract class H2Operations<D, R> implements AutoCloseable {
    protected H2ResultsStorage<D, R>.Connection con;
    protected PreparedStatement stmt;

    public H2Operations(H2ResultsStorage<D, R>.Connection con) {
        this.con = con;
    }

    @Override
    public void close() throws SQLException {
        stmt.close();
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
}
