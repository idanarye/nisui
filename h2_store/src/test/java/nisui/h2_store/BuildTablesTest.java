package nisui.h2_store;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.rules.TemporaryFolder;

import nisui.core.DataPointInserter;
import nisui.core.DynamicExperimentValue;
import nisui.core.DynamicExperimentValueHandler;
import nisui.core.ExperimentValuesHandler;
import org.junit.*;

public class BuildTablesTest {
	@Rule
	public TemporaryFolder dbFolder = new TemporaryFolder();

	private String tmpDbFileName() {
		try {
			return dbFolder.newFile("test-db").getAbsolutePath();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void createTables() throws SQLException {
		H2ResultsStorage<?, ?> storage = new H2ResultsStorage<>(tmpDbFileName(),
				new DynamicExperimentValueHandler().addField("a", int.class),
				new DynamicExperimentValueHandler().addField("x", double.class));
		storage.prepareStorage();
		try (H2ResultsStorage<?, ?>.Connection con = storage.connect()) {
			try (ResultSet rs = con.createPreparedStatement("SHOW COLUMNS FROM %s;", con.DATA_POINTS_TABLE_NAME).executeQuery()) {
				rs.next();
				assert rs.getString(1).equalsIgnoreCase("id");

				rs.next();
				assert rs.getString(1).equalsIgnoreCase("a");
				assert rs.getString(2).toUpperCase().startsWith("INTEGER");

				assert !rs.next();
			}

			try (ResultSet rs = con.createPreparedStatement("SHOW COLUMNS FROM %s;", con.EXPERIMENT_RESULTS_TABLE_NAME).executeQuery()) {
				rs.next();
				assert rs.getString(1).equalsIgnoreCase("id");

				rs.next();
				assert rs.getString(1).equalsIgnoreCase("data_point_id");
				assert rs.getString(2).toUpperCase().startsWith("BIGINT");

				rs.next();
				assert rs.getString(1).equalsIgnoreCase("seed");
				assert rs.getString(2).toUpperCase().startsWith("BIGINT");

				rs.next();
				assert rs.getString(1).equalsIgnoreCase("x");
				assert rs.getString(2).toUpperCase().startsWith("DOUBLE");

				assert !rs.next();
			}
		}
	}

	@Test
	public void addFields() throws SQLException {
		String filename = tmpDbFileName();
		H2ResultsStorage<?, ?> storage = new H2ResultsStorage<>(filename,
				new DynamicExperimentValueHandler().addField("a", int.class),
				new DynamicExperimentValueHandler().addField("x", double.class));
		storage.prepareStorage();

		storage = new H2ResultsStorage<>(filename,
				new DynamicExperimentValueHandler().addField("a", int.class).addField("b", double.class),
				new DynamicExperimentValueHandler().addField("x", double.class));
		storage.prepareStorage();

		try (H2ResultsStorage<?, ?>.Connection con = storage.connect()) {
			try (ResultSet rs = con.createPreparedStatement("SHOW COLUMNS FROM %s;", con.DATA_POINTS_TABLE_NAME).executeQuery()) {
				rs.next();
				assert rs.getString(1).equalsIgnoreCase("id");

				rs.next();
				assert rs.getString(1).equalsIgnoreCase("a");
				assert rs.getString(2).toUpperCase().startsWith("INTEGER");

				rs.next();
				assert rs.getString(1).equalsIgnoreCase("b");
				assert rs.getString(2).toUpperCase().startsWith("DOUBLE");

				assert !rs.next();
			}
		}
	}

	@Test
	public void removeFields() throws SQLException {
		String filename = tmpDbFileName();
		H2ResultsStorage<?, ?> storage = new H2ResultsStorage<>(filename,
				new DynamicExperimentValueHandler().addField("a", int.class).addField("b", double.class),
				new DynamicExperimentValueHandler().addField("x", double.class));
		storage.prepareStorage();

		storage = new H2ResultsStorage<>(filename,
				new DynamicExperimentValueHandler().addField("a", int.class),
				new DynamicExperimentValueHandler().addField("x", double.class));
		storage.prepareStorage();

		try (H2ResultsStorage<?, ?>.Connection con = storage.connect()) {
			try (ResultSet rs = con.createPreparedStatement("SHOW COLUMNS FROM %s;", con.DATA_POINTS_TABLE_NAME).executeQuery()) {
				rs.next();
				assert rs.getString(1).equalsIgnoreCase("id");

				rs.next();
				assert rs.getString(1).equalsIgnoreCase("a");
				assert rs.getString(2).toUpperCase().startsWith("INTEGER");

				assert !rs.next();
			}
		}
	}

	@Test
	public void changeFields() throws SQLException {
		String filename = tmpDbFileName();
		H2ResultsStorage<?, ?> storage = new H2ResultsStorage<>(filename,
				new DynamicExperimentValueHandler().addField("a", int.class).addField("b", double.class),
				new DynamicExperimentValueHandler().addField("x", double.class));
		storage.prepareStorage();

		storage = new H2ResultsStorage<>(filename,
				new DynamicExperimentValueHandler().addField("a", int.class).addField("b", String.class),
				new DynamicExperimentValueHandler().addField("x", double.class));
		storage.prepareStorage();

		try (H2ResultsStorage<?, ?>.Connection con = storage.connect()) {
			try (ResultSet rs = con.createPreparedStatement("SHOW COLUMNS FROM %s;", con.DATA_POINTS_TABLE_NAME).executeQuery()) {
				rs.next();
				assert rs.getString(1).equalsIgnoreCase("id");

				rs.next();
				assert rs.getString(1).equalsIgnoreCase("a");
				assert rs.getString(2).toUpperCase().startsWith("INTEGER");

				rs.next();
				assert rs.getString(1).equalsIgnoreCase("b");
				assert rs.getString(2).toUpperCase().startsWith("VARCHAR");

				assert !rs.next();
			}
		}
	}

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
			con.print(System.err, "SELECT * FROM %s;", con.DATA_POINTS_TABLE_NAME);
		}
	}
}

