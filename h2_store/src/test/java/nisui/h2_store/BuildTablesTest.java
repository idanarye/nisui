package nisui.h2_store;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.rules.TemporaryFolder;

import lombok.*;
import nisui.core.ExperimentValuesHandler;
import nisui.java_runner.JavaExperimentValuesHandler;
import org.junit.*;

@Data
class TestDataPoint1 implements nisui.core.DataPoint {
	private int a;
}
@Data
class TestDataPoint2 implements nisui.core.DataPoint {
	private int a;
	private double b;
}
@Data
class TestDataPoint3 implements nisui.core.DataPoint {
	private int a;
	private String b;
}

@Data
class TestExperimentResult implements nisui.core.ExperimentResult {
	private double x;
}

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
		H2ResultsStorage storage = new H2ResultsStorage(tmpDbFileName(), new JavaExperimentValuesHandler<>(TestDataPoint1.class), new JavaExperimentValuesHandler<>(TestExperimentResult.class));
		storage.prepareStorage();
		try (H2Connection con = storage.connect()) {
			try (ResultSet rs = con.statement("SHOW COLUMNS FROM data_points;").executeQuery()) {
				rs.next();
				assert rs.getString(1).equalsIgnoreCase("id");

				rs.next();
				assert rs.getString(1).equalsIgnoreCase("a");
				assert rs.getString(2).toUpperCase().startsWith("INTEGER");

				assert !rs.next();
			}

			try (ResultSet rs = con.statement("SHOW COLUMNS FROM experiment_results;").executeQuery()) {
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
		H2ResultsStorage storage = new H2ResultsStorage(filename, new JavaExperimentValuesHandler<>(TestDataPoint1.class), new JavaExperimentValuesHandler<>(TestExperimentResult.class));
		storage.prepareStorage();

		storage = new H2ResultsStorage(filename, new JavaExperimentValuesHandler<>(TestDataPoint2.class), new JavaExperimentValuesHandler<>(TestExperimentResult.class));
		storage.prepareStorage();

		try (H2Connection con = storage.connect()) {
			try (ResultSet rs = con.statement("SHOW COLUMNS FROM data_points;").executeQuery()) {
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
		H2ResultsStorage storage = new H2ResultsStorage(filename, new JavaExperimentValuesHandler<>(TestDataPoint2.class), new JavaExperimentValuesHandler<>(TestExperimentResult.class));
		storage.prepareStorage();

		storage = new H2ResultsStorage(filename, new JavaExperimentValuesHandler<>(TestDataPoint1.class), new JavaExperimentValuesHandler<>(TestExperimentResult.class));
		storage.prepareStorage();

		try (H2Connection con = storage.connect()) {
			try (ResultSet rs = con.statement("SHOW COLUMNS FROM data_points;").executeQuery()) {
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
		H2ResultsStorage storage = new H2ResultsStorage(filename, new JavaExperimentValuesHandler<>(TestDataPoint2.class), new JavaExperimentValuesHandler<>(TestExperimentResult.class));
		storage.prepareStorage();

		storage = new H2ResultsStorage(filename, new JavaExperimentValuesHandler<>(TestDataPoint3.class), new JavaExperimentValuesHandler<>(TestExperimentResult.class));
		storage.prepareStorage();

		try (H2Connection con = storage.connect()) {
			try (ResultSet rs = con.statement("SHOW COLUMNS FROM data_points;").executeQuery()) {
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

