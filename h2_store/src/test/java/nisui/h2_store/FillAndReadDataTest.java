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
	@Test
	public void addDataPoints() throws SQLException {
		DynamicExperimentValueHandler dph = new DynamicExperimentValueHandler()
			.addField("a", int.class)
			.addField("b", double.class);
		H2ResultsStorage<DynamicExperimentValue, DynamicExperimentValue> storage = new H2ResultsStorage<>(tmpDbFileName(),
				dph,
				new DynamicExperimentValueHandler().addField("x", double.class));
		storage.prepareStorage();

		try (H2ResultsStorage<DynamicExperimentValue, ?>.Connection con = storage.connect()) {
			try (H2Operations.InsertDataPoint<DynamicExperimentValue, ?> inserter = con.insertDataPoints()) {
				DynamicExperimentValue dp = dph.createValue();
				dp.set("a", 12);
				dp.set("b", 1.12);
				inserter.insert(dp);

				dp = dph.createValue();
				dp.set("a", 15);
				dp.set("b", 2.15);
				inserter.insert(dp);

				dp = dph.createValue();
				dp.set("a", 20);
				dp.set("b", 3.2);
				inserter.insert(dp);
			}
		}

		try (H2ResultsStorage<DynamicExperimentValue, ?>.Connection con = storage.connect()) {
			try (H2Operations.ReadDataPoints<DynamicExperimentValue, ?> reader = con.readDataPoints()) {
				for (DataPoint<DynamicExperimentValue> dataPoint : reader) {
					switch ((int)((H2DataPoint<?>)dataPoint).getId()) {
						case 1:
							Assertions.assertThat(dataPoint.getValue().get("a")).isEqualTo(12);
							Assertions.assertThat(dataPoint.getValue().get("b")).isEqualTo(1.12);
							break;
						case 2:
							Assertions.assertThat(dataPoint.getValue().get("a")).isEqualTo(15);
							Assertions.assertThat(dataPoint.getValue().get("b")).isEqualTo(2.15);
							break;
						case 3:
							Assertions.assertThat(dataPoint.getValue().get("a")).isEqualTo(20);
							Assertions.assertThat(dataPoint.getValue().get("b")).isEqualTo(3.2);
							break;
						default:
							assert false;
					}
				}
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
				inserter.insert(dp);

				dp = dph.createValue();
				dp.set("a", 2);
				inserter.insert(dp);
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
				for (ExperimentResult<DynamicExperimentValue, DynamicExperimentValue> experimentResult : reader) {
					switch ((int)((H2ExperimentResult<?, ?>)experimentResult).getId()) {
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
			}
		}
	}
}

