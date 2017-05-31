package nisui.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.junit.*;

public class BasicExperimentRunningTest {
	private static Logger logger = LoggerFactory.getLogger(BasicExperimentRunningTest.class);

	@Test
	public void simpleExperimentRunner() {
		class MyDataPoint implements DataPoint {
			public int tensDigit;
			public int unitsDigit;
		}
		class MyResult implements ExperimentResult {
			public int number;
		}
		class MyExperiment implements ExperimentFunction<MyDataPoint, MyResult> {
			@Override public MyResult runExperiment(long seed, MyDataPoint dataPoint) {
				MyResult result = new MyResult();
				result.number = 10 * dataPoint.tensDigit + 1 * dataPoint.unitsDigit;
				return result;
			}
		}


		MyExperiment experiment = new MyExperiment();
		ExperimentsRunner<MyDataPoint, MyResult> handler = new
			ExperimentsRunner<MyDataPoint, MyResult>(experiment);
		logger.info("handler.dataPointClass = {}", handler.dataPointClass);
		logger.info("handler.experimentResultClass = {}", handler.experimentResultClass);
		//MyDataPoint dataPoint = new MyDataPoint();
		//dataPoint.tensDigit = 4;
		//dataPoint.unitsDigit = 2;
		//logger.info("Running for tensDigit={}, unitsDigit={}", dataPoint.tensDigit, dataPoint.unitsDigit);
		//MyResult result = experiment.runExperiment(0, dataPoint);
		//logger.info("Got result number={}", result.number);
		//assert result.number == 42;
	}
}
