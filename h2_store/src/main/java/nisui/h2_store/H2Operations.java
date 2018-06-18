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
        ReadDataPoints(H2ResultsStorage<D, R>.Connection con) {
            super(con);
        }

        @Override
        public RSIterator iterator() {
            if (stmt == null) {
                StringBuilder sql = new StringBuilder();
                sql.append("SELECT id, num_planned, num_performed");
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
                        int i = 4;
                        for (ExperimentValuesHandler<D>.Field field : con.parent().dataPointHandler.fields()) {
                            field.set(value, fromDbRep(field, rs.getObject(i)));
                            ++i;
                        }
                        next = new H2DataPoint<>(rs.getLong(1), rs.getLong(2), rs.getLong(3), value);
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
        public RSIterator iterator() {
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
                return new RSIterator(stmt.executeQuery());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        private class RSIterator implements Iterator<ExperimentResult<D, R>> {
            private ResultSet rs;
            private H2ExperimentResult<D, R> next;

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
            public H2ExperimentResult<D, R> next() {
                try {
                    H2ExperimentResult<D, R> currentNext = next;
                    if (rs.next()) {
                        R value = con.parent().experimentResultHandler.createValue();
                        int i = 4;
                        for (ExperimentValuesHandler<R>.Field field : con.parent().experimentResultHandler.fields()) {
                            field.set(value, fromDbRep(field, rs.getObject(i)));
                            ++i;
                        }
                        next = new H2ExperimentResult<>(rs.getLong(1), dataPoints.get(rs.getLong(2)), rs.getLong(3), value);
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

    public static class RunQuery<D, R> extends H2Operations<D, R> implements QueryRunner<D> {
        private HashMap<Long, H2DataPoint<D>> dataPoints;
        private String[] queries;
        private String[] sortBy;

        RunQuery(H2ResultsStorage<D, R>.Connection con, Iterable<DataPoint<D>> dataPoints, String[] queries, String[] sortBy) {
            super(con);
            this.dataPoints = new HashMap<>();
            for (DataPoint<D> dp : dataPoints) {
                H2DataPoint<D> h2dp = (H2DataPoint<D>)dp;
                this.dataPoints.put(h2dp.getId(), h2dp);
            }
            this.queries = queries;
            this.sortBy = sortBy;
        }

        @Override
        public RSIterator iterator() {
            LinkedList<Object> parameters = new LinkedList<>();
            if (stmt == null) {
                StringBuilder sql = new StringBuilder();
                sql.append("SELECT MIN(data_point_id)");
                H2QueryParser queryParser = new H2QueryParser();
                for (String query : queries) {
                    sql.append(", (");
                    queryParser.parseString(query).scan(sql::append, parameters::add);
                    sql.append(")");
                }
                sql.append(" FROM ").append(con.EXPERIMENT_RESULTS_TABLE_NAME).append(" AS er");
                sql.append(" INNER JOIN ").append(con.DATA_POINTS_TABLE_NAME).append(" AS dp ON er.data_point_id = dp.id");
                sql.append(" WHERE data_point_id IN (SELECT * FROM TABLE(id BIGINT = ?))");

                sql.append(" GROUP BY ");
                IterWithSeparator.iterWithSep(
                        con.parent().dataPointHandler.fields(),
                        field -> sql.append("dp." + field.getName()),
                        () -> sql.append(", "));

                HashMap<String, Integer> sortByIndex = new HashMap<>();
                for (int i = 0; i < sortBy.length; ++i) {
                    sortByIndex.put(sortBy[i], i);
                }

                sql.append(" ORDER BY ");
                IterWithSeparator.iterWithSep(
                        con.parent().dataPointHandler.fields().stream().sorted(
                            (a, b) -> Integer.compare(
                                sortByIndex.getOrDefault(b, sortBy.length),
                                sortByIndex.getOrDefault(a, sortBy.length))),
                        field -> sql.append("dp." + field.getName()),
                        () -> sql.append(", "));


                sql.append(';');
                stmt = con.createPreparedStatement(sql.toString());
            }
            try {
                Array array = stmt.getConnection().createArrayOf("BIGINT", dataPoints.keySet().toArray());
                stmt.setArray(1, array);
                return new RSIterator(stmt.executeQuery());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        private class RSIterator implements Iterator<RunQuery.Row<D>> {
            private ResultSet rs;
            private RunQuery.Row<D> next;

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
            public RunQuery.Row<D> next() {
                try {
                    RunQuery.Row<D> currentNext = next;
                    if (rs.next()) {
                        R value = con.parent().experimentResultHandler.createValue();
                        double[] values = new double[queries.length];
                        for (int i = 0; i < values.length; ++i) {
                            values[i] = rs.getDouble(i + 2);
                        }
                        next = new RunQuery.Row<>(dataPoints.get(rs.getLong(1)).getValue(), values);
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
