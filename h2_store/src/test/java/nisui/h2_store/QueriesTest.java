package nisui.h2_store;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.junit.Test;

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
			}

			try (H2Operations.InsertDataPoint<DynamicExperimentValue, ?> inserter = con.insertDataPoints()) {
				inserter.insert(1, 1, erh.createValue(1));
				inserter.insert(1, 2, erh.createValue(2));
				inserter.insert(1, 3, erh.createValue(3));
				inserter.insert(1, 4, erh.createValue(4));
			}

			H2Query<DynamicExperimentValue> query = storage.createQuery("SUM(x + 12) - AVG(LOG2(x))");
			System.out.println(query.sql);
		}
	}
}
