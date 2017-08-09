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
class TestDataPoint implements nisui.core.DataPoint {
	private int a;
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
	public void test1() throws SQLException {
		H2ResultsStorage storage = new H2ResultsStorage(tmpDbFileName(), new JavaExperimentValuesHandler<>(TestDataPoint.class), new JavaExperimentValuesHandler<>(TestExperimentResult.class));
		storage.prepareStorage();
		try (H2Connection con = storage.connect()) {
			con.print(System.err, "SHOW COLUMNS FROM data_points;");
			try (ResultSet rs = con.statement("SHOW COLUMNS FROM data_points;").executeQuery()) {
				rs.next();
				assert rs.getString(1).equalsIgnoreCase("id");

				rs.next();
				assert rs.getString(1).equalsIgnoreCase("a");

				assert !rs.next();
			}
		}
	}
}

