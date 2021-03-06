package nisui.h2_store;

import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nisui.core.*;
import nisui.core.util.IterWithSeparator;
import nisui.core.util.QueryChunk;
import nisui.core.util.ResultSetIterator;

public abstract class H2Operations<D, R> implements AutoCloseable {
    private static Logger logger = LoggerFactory.getLogger(H2Operations.class);
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

    private static <V> Object toDbRep(ExperimentValuesHandler<V>.Field field, V experimentValue) {
        Object value = field.get(experimentValue);
        if (value != null && field.getType().isEnum()) {
            value = value.toString();
        }
        return value;
    }

    private static <V> Object fromDbRep(ExperimentValuesHandler<V>.Field field, Object dbRep) {
        if (dbRep instanceof String) {
            return field.parseString((String)dbRep);
        } else {
            return dbRep;
        }
    }

    private static void applyQueryChunk(QueryChunk chunk, StringBuilder sql, List<Object> parameters) {
        chunk.scan(sql::append, (param) -> {
            sql.append(" ? ");
            parameters.add(param);
        });
    }

    public static class InsertDataPoint<D, R> extends H2Operations<D, R> implements DataPointInserter<D> {
        InsertDataPoint(H2ResultsStorage<D, R>.Connection con) {
            super(con);
            StringBuilder sql = new StringBuilder();
            sql.append("INSERT INTO ").append(con.DATA_POINTS_TABLE_NAME).append("(");
            sql.append("num_planned").append(", ").append("num_performed");
            for (ExperimentValuesHandler<D>.Field field : con.parent().dataPointHandler.fields()) {
                sql.append(", ").append(field.getName());
            }
            sql.append(") VALUES(");
            int numberOfFields = con.parent().dataPointHandler.fields().size();
            for (int i = 0; i < 2 + numberOfFields; ++i) {
                if (0 < i) {
                    sql.append(", ");
                }
                sql.append("?");
            }
            sql.append(");");
            stmt = con.createPreparedStatement(sql.toString());
        }

        @Override
        public void insert(long numPlanned, long numPerformed, D dataPoint) throws SQLException {
            stmt.clearParameters();
            stmt.setLong(1, numPlanned);
            stmt.setLong(2, numPerformed);
            int paramIndex = 3;
            for (ExperimentValuesHandler<D>.Field field : con.parent().dataPointHandler.fields()) {
                stmt.setObject(paramIndex, toDbRep(field, dataPoint));
                ++paramIndex;
            }
            stmt.executeUpdate();
        }
    }

    public static class ReadDataPoints<D, R> extends H2Operations<D, R> implements DataPointsReader<D> {
        String[] filters;

        ReadDataPoints(H2ResultsStorage<D, R>.Connection con, String... filters) {
            super(con);
            this.filters = filters;
        }

        @Override
        public Iterator<DataPoint<D>> iterator() {
            LinkedList<Object> parameters = new LinkedList<>();
            if (stmt == null) {
                StringBuilder sql = new StringBuilder();
                sql.append("SELECT id, num_planned, num_performed");
                for (ExperimentValuesHandler<D>.Field field : con.parent().dataPointHandler.fields()) {
                    sql.append(", ").append(field.getName());
                }
                sql.append(" FROM ").append(con.DATA_POINTS_TABLE_NAME);
                if (0 < filters.length) {
                    H2QueryParser queryParser = new H2QueryParser();
                    for (int i = 0; i < filters.length; ++i) {
                        if (0 == i) {
                            sql.append(" WHERE ");
                        } else {
                            sql.append(" AND ");
                        }
                        sql.append("(");
                        applyQueryChunk(queryParser.parseBoolean(filters[i]), sql, parameters);
                        sql.append(")");
                    }
                }
                sql.append(';');
                stmt = con.createPreparedStatement(sql.toString());
            }
            try {
                {
                    int i = 1;
                    for (Object parameter : parameters) {
                        stmt.setObject(i, parameter);
                        i += 1;
                    }
                }
                return new ResultSetIterator<>(stmt.executeQuery(), rs -> {
                    D value = con.parent().dataPointHandler.createValue();
                    int i = 4;
                    for (ExperimentValuesHandler<D>.Field field : con.parent().dataPointHandler.fields()) {
                        field.set(value, fromDbRep(field, rs.getObject(i)));
                        ++i;
                    }
                    return new H2DataPoint<>(rs.getLong(1), rs.getLong(2), rs.getLong(3), value);
                });
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class InsertExperimentResult<D, R> extends H2Operations<D, R> implements ExperimentResultInserter<R> {
        private PreparedStatement updateDpStatement;

        @Override
        public void close() throws SQLException {
            try {
                if (updateDpStatement != null) {
                    updateDpStatement.close();
                }
            } finally {
                super.close();
            }
        }

        InsertExperimentResult(H2ResultsStorage<D, R>.Connection con) {
            super(con);
            StringBuilder sql = new StringBuilder();
            sql.append("INSERT INTO ").append(con.EXPERIMENT_RESULTS_TABLE_NAME).append("(");
            sql.append("data_point_id").append(", ").append("seed");
            for (ExperimentValuesHandler<R>.Field field : con.parent().experimentResultHandler.fields()) {
                sql.append(", ").append(field.getName());
            }
            sql.append(") VALUES(");
            int numberOfFields = con.parent().experimentResultHandler.fields().size();
            for (int i = 0; i < 2 + numberOfFields; ++i) {
                if (0 < i) {
                    sql.append(", ");
                }
                sql.append("?");
            }
            sql.append(");");
            stmt = con.createPreparedStatement(sql.toString());
            updateDpStatement = con.createPreparedStatement(String.format("UPDATE %s SET num_performed = num_performed + 1 WHERE id = ?;", con.DATA_POINTS_TABLE_NAME));
        }

        @Override
        public void insert(DataPoint<?> dataPoint, long seed, R experimentResult) throws SQLException {
            stmt.clearParameters();
            stmt.setLong(1, ((H2DataPoint<?>)dataPoint).getId());
            stmt.setLong(2, seed);
            int paramIndex = 3;
            for (ExperimentValuesHandler<R>.Field field : con.parent().experimentResultHandler.fields()) {
                stmt.setObject(paramIndex, toDbRep(field, experimentResult));
                ++paramIndex;
            }
            updateDpStatement.setLong(1, ((H2DataPoint<?>)dataPoint).getId());
            stmt.executeUpdate();
            updateDpStatement.executeUpdate();
        }
    }

    public static class ReadExperimentResults<D, R> extends H2Operations<D, R> implements ExperimentResultsReader<D, R> {
        private HashMap<Long, H2DataPoint<D>> dataPoints;

        ReadExperimentResults(H2ResultsStorage<D, R>.Connection con, Iterable<DataPoint<D>> dataPoints) {
            super(con);
            this.dataPoints = new HashMap<>();
            for (DataPoint<D> dp : dataPoints) {
                H2DataPoint<D> h2dp = (H2DataPoint<D>)dp;
                this.dataPoints.put(h2dp.getId(), h2dp);
            }
        }

        @Override
        public Iterator<ExperimentResult<D, R>> iterator() {
            if (stmt == null) {
                StringBuilder sql = new StringBuilder();
                sql.append("SELECT id");
                sql.append(", ").append("data_point_id");
                sql.append(", ").append("seed");
                for (ExperimentValuesHandler<R>.Field field : con.parent().experimentResultHandler.fields()) {
                    sql.append(", ").append(field.getName());
                }
                sql.append(" FROM ").append(con.EXPERIMENT_RESULTS_TABLE_NAME);
                sql.append(" WHERE data_point_id IN (SELECT * FROM TABLE(id BIGINT = ?))");
                sql.append(';');
                stmt = con.createPreparedStatement(sql.toString());
            }
            try {
                Array array = stmt.getConnection().createArrayOf("BIGINT", dataPoints.keySet().toArray());
                stmt.setArray(1, array);
                return new ResultSetIterator<>(stmt.executeQuery(), rs -> {
                    R value = con.parent().experimentResultHandler.createValue();
                    int i = 4;
                    for (ExperimentValuesHandler<R>.Field field : con.parent().experimentResultHandler.fields()) {
                        field.set(value, fromDbRep(field, rs.getObject(i)));
                        ++i;
                    }
                    return new H2ExperimentResult<>(rs.getLong(1), dataPoints.get(rs.getLong(2)), rs.getLong(3), value);
                });
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
