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

public abstract class TestsBase {
	@Rule
	public TemporaryFolder dbFolder = new TemporaryFolder();

	protected String tmpDbFileName() {
		try {
			return dbFolder.newFile("test-db").getAbsolutePath();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}

