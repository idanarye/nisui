package nisui.core;

import java.util.List;

public abstract class ResultsStorage<D, R> {
    public abstract void prepareStorage();
    public abstract Connection connect();

    public abstract class Connection implements AutoCloseable {
        public abstract DataPointInserter<D> insertDataPoints();
        public abstract DataPointsReader<D> readDataPoints(String... filters);
        public DataPointsReader<D> readDataPoints(List<DataPoint<D>> filters) {
            return readDataPoints(filters.toArray(String[]::new));
        }

        public abstract ExperimentResultInserter<R> insertExperimentResults();
        public abstract ExperimentResultsReader<D, R> readExperimentResults(Iterable<DataPoint<D>> dataPoints);

        public abstract QueryRunner<D> runQuery(Iterable<DataPoint<D>> dataPoints, String[] queries, String[] groupBy);
        public QueryRunner<D> runQuery(Iterable<DataPoint<D>> dataPoints, List<String> queries, List<String> groupBy) {
            return runQuery(dataPoints, queries.toArray(String[]::new), groupBy.toArray(String[]::new));
        }

        public abstract StoredPlotSaver saveStoredPlots();
        public abstract StoredPlotsReader readStoredPlots();
    }

    public abstract ExperimentValuesHandler<D> getDataPointHandler();
    public abstract ExperimentValuesHandler<R> getExperimentResultHandler();
}
