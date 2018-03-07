package nisui.simple_reactor;

import nisui.core.ExperimentValuesHandler;

public class ExperimentFailedException extends Exception {
        private static final long serialVersionUID = 1L;

    private ExperimentValuesHandler dataPointHandler;
    private Object dataPoint;
    private long seed;

    private static <T> String formatMessage(ExperimentValuesHandler<T> dataPointHandler, T dataPoint, long seed) {
        StringBuilder result = new StringBuilder();
        result.append("Experiment failed on data-point (");
        boolean isFirst = true;
        for (ExperimentValuesHandler<T>.Field field : dataPointHandler.fields()) {
            if (isFirst) {
                isFirst = false;
            } else {
                result.append(", ");
            }
            result.append(field.getName());
            result.append('=');
            result.append(field.get(dataPoint));
        }
        result.append(") and seed=");
        result.append(seed);
        return result.toString();
    }

    public <T> ExperimentFailedException(ExperimentValuesHandler<T> dataPointHandler, T dataPoint, long seed, Throwable cause) {
        super(formatMessage(dataPointHandler, dataPoint, seed), cause);
        this.dataPointHandler = dataPointHandler;
        this.dataPoint = dataPoint;
        this.seed = seed;
    }

    public ExperimentValuesHandler getDataPointHandler() {
        return dataPointHandler;
    }

    public Object getDataPoint() {
        return dataPoint;
    }

    public long getSeed() {
        return seed;
    }
}
