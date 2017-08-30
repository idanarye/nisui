package nisui.h2_store;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.rules.TemporaryFolder;

import nisui.core.DataPoint;
import nisui.core.DataPointInserter;
import nisui.core.DynamicExperimentValue;
import nisui.core.DynamicExperimentValueHandler;
import nisui.core.ExperimentValuesHandler;
import org.junit.*;

public class BuildTablesTest extends TestsBase {

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
}

