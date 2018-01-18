package nisui.app;

import java.net.URI;
import java.net.URISyntaxException;
import nisui.core.*;
import nisui.java_runner.JavaExperimentFunction;

public class NisuiConfig implements NisuiFactory {
    public String database;
    public static class Experiment {
        public String entry;
        public String[] dependencies;
    }
    public Experiment experiment;

    private static URI makeUri(String source) throws URISyntaxException {
        URI uri = new URI(source);
        if (uri.getScheme() == null) {
            uri = new URI("file", uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(), uri.getQuery(), uri.getFragment());
        }
        return uri;
    }

    @Override
    public ExperimentFunction createExperimentFunction() {
        URI entry = null;
        URI[] dependencies = new URI[experiment.dependencies.length];
        try {
            entry = makeUri(experiment.entry);
            for (int i = 0; i < dependencies.length; ++i) {
                dependencies[i] = makeUri(experiment.dependencies[i]);
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return JavaExperimentFunction.load(entry, dependencies);
    }

    @Override
    public ResultsStorage createResultsStorage() {
        return null;
    }
}
