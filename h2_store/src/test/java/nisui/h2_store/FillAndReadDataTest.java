package nisui.h2_store;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.rules.TemporaryFolder;
import org.assertj.core.api.Assertions;
import nisui.core.DataPoint;
import nisui.core.DataPointInserter;
import nisui.core.DynamicExperimentValue;
import nisui.core.DynamicExperimentValueHandler;
import nisui.core.ExperimentResult;
import nisui.core.ExperimentValuesHandler;
import org.junit.*;

public class FillAndReadDataTest extends TestsBase {
    enum MyEnum { X, Y, Z }

    @Test
    public void addDataPoints() throws SQLException {
        DynamicExperimentValueHandler dph = new DynamicExperimentValueHandler()
            .addField("a", int.class)
            .addField("b", double.class)
            .addField("c", MyEnum.class);
        H2ResultsStorage<DynamicExperimentValue, DynamicExperimentValue> storage = new H2ResultsStorage<>(tmpDbFileName(),
                dph,
                new DynamicExperimentValueHandler().addField("x", double.class));
        storage.prepareStorage();

        try (H2ResultsStorage<DynamicExperimentValue, ?>.Connection con = storage.connect()) {
            try (H2Operations.InsertDataPoint<DynamicExperimentValue, ?> inserter = con.insertDataPoints()) {
                DynamicExperimentValue dp = dph.createValue();
                dp.set("a", 12);
                dp.set("b", 1.12);
                dp.set("c", MyEnum.X);
                inserter.insert(1, 0, dp);

                dp = dph.createValue();
                dp.set("a", 15);
                dp.set("b", 2.15);
                dp.set("c", MyEnum.Y);
                inserter.insert(2, 0, dp);

                dp = dph.createValue();
                dp.set("a", 20);
                dp.set("b", 3.2);
                dp.set("c", MyEnum.Z);
                inserter.insert(3, 1, dp);
            }
        }

        try (H2ResultsStorage<DynamicExperimentValue, ?>.Connection con = storage.connect()) {
            try (H2Operations.ReadDataPoints<DynamicExperimentValue, ?> reader = con.readDataPoints()) {
                int lastId = 0;
                for (DataPoint<DynamicExperimentValue> dataPoint : reader) {
                    lastId = (int)((H2DataPoint<?>)dataPoint).getId();
                    switch (lastId) {
                        case 1:
                            Assertions.assertThat(dataPoint.getValue().get("a")).isEqualTo(12);
                            Assertions.assertThat(dataPoint.getValue().get("b")).isEqualTo(1.12);
                            Assertions.assertThat(dataPoint.getValue().get("c")).isEqualTo(MyEnum.X);
                            break;
                        case 2:
                            Assertions.assertThat(dataPoint.getValue().get("a")).isEqualTo(15);
                            Assertions.assertThat(dataPoint.getValue().get("b")).isEqualTo(2.15);
                            Assertions.assertThat(dataPoint.getValue().get("c")).isEqualTo(MyEnum.Y);
                            break;
                        case 3:
                            Assertions.assertThat(dataPoint.getValue().get("a")).isEqualTo(20);
                            Assertions.assertThat(dataPoint.getValue().get("b")).isEqualTo(3.2);
                            Assertions.assertThat(dataPoint.getValue().get("c")).isEqualTo(MyEnum.Z);
                            break;
                        default:
                            assert false;
                    }
                }
                Assertions.assertThat(lastId).isEqualTo(3);
            }
        }
    }

    @Test
    public void addExperimentResults() throws SQLException {
        DynamicExperimentValueHandler dph = new DynamicExperimentValueHandler()
            .addField("a", int.class);
        DynamicExperimentValueHandler erh = new DynamicExperimentValueHandler()
            .addField("x", int.class);
        H2ResultsStorage<DynamicExperimentValue, DynamicExperimentValue> storage = new H2ResultsStorage<>(tmpDbFileName(), dph, erh);
        storage.prepareStorage();

        try (H2ResultsStorage<DynamicExperimentValue, DynamicExperimentValue>.Connection con = storage.connect()) {
            try (H2Operations.InsertDataPoint<DynamicExperimentValue, ?> inserter = con.insertDataPoints()) {
                DynamicExperimentValue dp = dph.createValue();
                dp.set("a", 1);
                inserter.insert(1, 0, dp);

                dp = dph.createValue();
                dp.set("a", 2);
                inserter.insert(1, 0, dp);
            }

            long seed = 1;
            try (H2Operations.InsertExperimentResult<DynamicExperimentValue, DynamicExperimentValue> inserter = con.insertExperimentResults()) {;
                try (H2Operations.ReadDataPoints<DynamicExperimentValue, ?> reader = con.readDataPoints()) {
                    for (DataPoint<DynamicExperimentValue> dataPoint : reader) {
                        DynamicExperimentValue er = erh.createValue();
                        er.set("x", (int)dataPoint.getValue().get("a") * 10 + 1);
                        inserter.insert(dataPoint, seed, er);
                        ++ seed;

                        er = erh.createValue();
                        er.set("x", (int)dataPoint.getValue().get("a") * 10 + 2);
                        inserter.insert(dataPoint, seed, er);
                        ++ seed;
                    }
                }
            }

            try (H2Operations.ReadExperimentResults<DynamicExperimentValue, DynamicExperimentValue> reader = con.readExperimentResults(con.readDataPoints())) {
                int lastId = 0;
                for (ExperimentResult<DynamicExperimentValue, DynamicExperimentValue> experimentResult : reader) {
                    lastId = (int)((H2ExperimentResult<?, ?>)experimentResult).getId();
                    switch (lastId) {
                        case 1:
                            Assertions.assertThat(experimentResult.getDataPoint().getValue().get("a")).isEqualTo(1);
                            Assertions.assertThat(experimentResult.getSeed()).isEqualTo(1);
                            Assertions.assertThat(experimentResult.getValue().get("x")).isEqualTo(11);
                            break;
                        case 2:
                            Assertions.assertThat(experimentResult.getDataPoint().getValue().get("a")).isEqualTo(1);
                            Assertions.assertThat(experimentResult.getSeed()).isEqualTo(2);
                            Assertions.assertThat(experimentResult.getValue().get("x")).isEqualTo(12);
                            break;
                        case 3:
                            Assertions.assertThat(experimentResult.getDataPoint().getValue().get("a")).isEqualTo(2);
                            Assertions.assertThat(experimentResult.getSeed()).isEqualTo(3);
                            Assertions.assertThat(experimentResult.getValue().get("x")).isEqualTo(21);
                            break;
                        case 4:
                            Assertions.assertThat(experimentResult.getDataPoint().getValue().get("a")).isEqualTo(2);
                            Assertions.assertThat(experimentResult.getSeed()).isEqualTo(4);
                            Assertions.assertThat(experimentResult.getValue().get("x")).isEqualTo(22);
                            break;
                        default:
                            assert false;
                    }
                }
                Assertions.assertThat(lastId).isEqualTo(4);
            }
        }
    }
}

