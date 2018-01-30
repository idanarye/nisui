package nisui.app;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import nisui.core.*;
import nisui.h2_store.H2ResultsStorage;
import nisui.java_runner.JavaExperimentFunction;

public class NisuiConfig implements NisuiFactory {
    public String database;
    public static class Experiment {
        public String entry;
        public String[] dependencies;
    }
    public Experiment experiment;

    private static URI makeUri(String source) throws URISyntaxException, IOException {
        URI uri = new URI(source);
        if (uri.getScheme() == null) {
            uri = new URI(
                "file",
                uri.getUserInfo(),
                uri.getHost(),
                uri.getPort(),
                new File(uri.getPath()).getCanonicalPath(),
                uri.getQuery(),
                uri.getFragment()
            );
        }
        return uri;
    }

    private ExperimentFunction<?, ?> experimentFunction = null;

    @Override
    public ExperimentFunction<?, ?> createExperimentFunction() {
        if (experimentFunction == null) {
            URI entry = null;
            URI[] dependencies = new URI[experiment.dependencies == null ? 0 : experiment.dependencies.length];
            try {
                entry = makeUri(experiment.entry);
                for (int i = 0; i < dependencies.length; ++i) {
                    dependencies[i] = makeUri(experiment.dependencies[i]);
                }
            } catch (URISyntaxException | IOException e) {
                throw new RuntimeException(e);
            }
            experimentFunction = JavaExperimentFunction.load(entry, dependencies);
        }
        return experimentFunction;
    }

    @Override
    public ResultsStorage<?, ?> createResultsStorage() {
        ExperimentFunction<?, ?> experimentFunction = createExperimentFunction();
        String databasePath;
        try {
            databasePath = new File(database).getCanonicalPath();
            return new H2ResultsStorage<>(databasePath, experimentFunction.getDataPointHandler(), experimentFunction.getExperimentResultHandler());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
