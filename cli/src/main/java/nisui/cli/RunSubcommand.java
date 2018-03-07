package nisui.cli;

import java.io.PrintStream;
import nisui.core.NisuiFactory;
import picocli.CommandLine;

import java.io.InputStream;

import nisui.core.*;
import nisui.simple_reactor.ExperimentFailedException;
import nisui.simple_reactor.SimpleReactor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CommandLine.Command(
name = "run",
description = "Run the data-points we have prepared, storing the results in the database.")
public class RunSubcommand extends SubCommand {
    private static Logger logger = LoggerFactory.getLogger(RunSubcommand.class);

    protected NisuiFactory nisuiFactory;

    public RunSubcommand(NisuiFactory nisuiFactory) {
        this.nisuiFactory = nisuiFactory;
    }

    @Override
    public String[] getNames() {
        return new String[]{"run"};
    }

    @Override
    public void run(InputStream in, PrintStream out) {
        ResultsStorage<?, ?> storage = nisuiFactory.createResultsStorage();
        ExperimentFunction<?, ?> experimentFunction = nisuiFactory.createExperimentFunction();
        run(out, storage, experimentFunction);
    }

    private void onException(ExperimentFailedException e) {
        // logger.error(e.getMessage());
        logger.error(e.getMessage(), e.getCause());
        // e.getCause().printStackTrace();
        // e.printStackTrace();
    }

    public <D, R> void run(PrintStream out, ResultsStorage<D, R> storage, ExperimentFunction<?, ?> experimentFunction) {
        SimpleReactor<D, R> reactor = new SimpleReactor<D, R>(storage, experimentFunction, this::onException);
        reactor.run();
    }
}
