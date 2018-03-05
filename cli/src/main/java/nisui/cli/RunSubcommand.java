package nisui.cli;

import java.io.PrintStream;
import nisui.core.NisuiFactory;
import picocli.CommandLine;

import java.io.InputStream;

import nisui.core.*;
import nisui.simple_reactor.SimpleReactor;

@CommandLine.Command(
name = "run",
description = "Run the data-points we have prepared, storing the results in the database.")
public class RunSubcommand extends SubCommand {
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

    public <D, R> void run(PrintStream out, ResultsStorage<D, R> storage, ExperimentFunction<?, ?> experimentFunction) {
        SimpleReactor<D, R> reactor = new SimpleReactor<D, R>(storage, experimentFunction);
        reactor.run();
    }
}
