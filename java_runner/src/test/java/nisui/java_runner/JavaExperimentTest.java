package nisui.java_runner;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.assertj.core.api.Assertions;

import nisui.core.ExperimentValuesHandler;
import org.junit.*;

@SuppressWarnings("unchecked")
public class JavaExperimentTest {
    @Test
    public void testExperiment() {
        URL resource = getClass().getClassLoader().getResource("SimpleNisuiRunner.java");
        JavaExperimentFunction runner = null;
        try {
            runner = JavaExperimentFunction.load(resource.toURI());
        } catch (Exception e) {
            throw new Error(e);
        }

        Object dataPoint = runner.getDataPointHandler().createValue();
        runner.getDataPointHandler().field("a").set(dataPoint, 2);

        Object experimentResult = runner.runExperiment(dataPoint, 3);

        // Formula is 100 * seed + dataPoint.a
        Assertions.assertThat((long)runner.getExperimentResultHandler().field("x").get(experimentResult)).isEqualTo(302);
    }
}
