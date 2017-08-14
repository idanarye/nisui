package nisui.h2_store;

import nisui.core.ExperimentValuesHandler;
import nisui.core.ResultsStorage;

public class H2ResultsStorage extends ResultsStorage {
    private String filename;
    private ExperimentValuesHandler<?> dataPointHandler;
    private ExperimentValuesHandler<?> experimentResultHandler;

    public H2ResultsStorage(String filename, ExperimentValuesHandler<?> dataPointHandler, ExperimentValuesHandler<?> experimentResultHandler) {
        this.filename = filename;
        this.dataPointHandler = dataPointHandler;
        this.experimentResultHandler = experimentResultHandler;
    }

    @Override
    public void prepareStorage() {
        try (H2Connection con = connect()) {
            con.prepareTable("data_points", dataPointHandler);
            con.prepareTable("experiment_results", experimentResultHandler,
                    new H2FieldDefinition("data_point_id", long.class),
                    new H2FieldDefinition("seed", long.class));
        }
    }

    @Override
    public H2Connection connect() {
        return new H2Connection(filename);
    }
}
