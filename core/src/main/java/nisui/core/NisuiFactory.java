package nisui.core;

public interface NisuiFactory {
    public ExperimentFunction createExperimentFunction() throws ExperimentFunctionCreationException;
    public ResultsStorage createResultsStorage() throws ExperimentFunctionCreationException;
}
