package nisui.h2_store;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.HashMap;

import org.junit.Test;
import org.assertj.core.api.Assertions;

import nisui.core.DataPoint;
import nisui.core.DynamicExperimentValue;
import nisui.core.DynamicExperimentValueHandler;

public class QueriesTest extends TestsBase {
    @Test
    public void queries() throws SQLException {
        DynamicExperimentValueHandler dph = new DynamicExperimentValueHandler()
            .addField("a", int.class);
        DynamicExperimentValueHandler erh = new DynamicExperimentValueHandler()
            .addField("x", int.class);
        H2ResultsStorage<DynamicExperimentValue, DynamicExperimentValue> storage = new H2ResultsStorage<>(tmpDbFileName(), dph, erh);
        storage.prepareStorage();

        try (H2ResultsStorage<DynamicExperimentValue, DynamicExperimentValue>.Connection con = storage.connect()) {
            try (H2Operations.InsertDataPoint<DynamicExperimentValue, ?> inserter = con.insertDataPoints()) {
                inserter.insert(1, 0, dph.createValue(1));
                inserter.insert(1, 0, dph.createValue(2));
                inserter.insert(1, 0, dph.createValue(3));
                inserter.insert(1, 0, dph.createValue(4));
            }

            Function<String[], List<DataPoint<DynamicExperimentValue>>> findDataPoints = (filters) -> {
                LinkedList<DataPoint<DynamicExperimentValue>> dataPoints = new LinkedList<>();
                try (H2Operations.ReadDataPoints<DynamicExperimentValue, ?> reader = con.readDataPoints(filters)) {
                    for (DataPoint<DynamicExperimentValue> dataPoint : reader) {
                        dataPoints.add(dataPoint);
                    }
                } catch (SQLException e) {
                    throw new Error(e);
                }
                return dataPoints;
            };

            LinkedList<DataPoint<DynamicExperimentValue>> dataPoints = new LinkedList<>();
            try (H2Operations.ReadDataPoints<DynamicExperimentValue, ?> reader = con.readDataPoints()) {
                for (DataPoint<DynamicExperimentValue> dataPoint : reader) {
                    dataPoints.add(dataPoint);
                }
            }

            try (H2Operations.InsertExperimentResult<DynamicExperimentValue, DynamicExperimentValue> inserter = con.insertExperimentResults()) {
                inserter.insert(dataPoints.get(0), 1, erh.createValue(1));
                inserter.insert(dataPoints.get(0), 2, erh.createValue(2));
                inserter.insert(dataPoints.get(1), 3, erh.createValue(3));
                inserter.insert(dataPoints.get(1), 4, erh.createValue(4));
                inserter.insert(dataPoints.get(2), 5, erh.createValue(5));
                inserter.insert(dataPoints.get(2), 6, erh.createValue(6));
                inserter.insert(dataPoints.get(3), 7, erh.createValue(7));
                inserter.insert(dataPoints.get(3), 8, erh.createValue(8));
            }

            HashMap<Integer, double[]> results = new HashMap<>();
            try (H2Operations.RunQuery<DynamicExperimentValue, ?> query = con.runQuery(findDataPoints.apply(new String[]{"a <= 2"}), new String[]{
                "MIN(x)",
                "SUM(x + 2) + 2",
                "MIN(x) + SUM(x)",
            }, new String[]{"a"})) {
                for (H2Operations.RunQuery.Row<DynamicExperimentValue> row : query) {
                    results.put((int)row.dataPoint.get("a"), row.values);
                }
            }

            Assertions.assertThat(results.size()).isEqualTo(2);
            Assertions.assertThat(results.get(1)).containsExactly(1, 9, 4);
            Assertions.assertThat(results.get(2)).containsExactly(3, 13, 10);

            results = new HashMap<>();
            try (H2Operations.RunQuery<DynamicExperimentValue, ?> query = con.runQuery(findDataPoints.apply(new String[]{"1 < a", "a < 3"}), new String[]{
                "MIN(x)",
                "SUM(x + 2) + 2",
                "MIN(x) + SUM(x)",
            }, new String[]{"a"})) {
                for (H2Operations.RunQuery.Row<DynamicExperimentValue> row : query) {
                    results.put((int)row.dataPoint.get("a"), row.values);
                }
            }

            Assertions.assertThat(results.size()).isEqualTo(1);
            Assertions.assertThat(results.get(2)).containsExactly(3, 13, 10);
        }
    }
}
