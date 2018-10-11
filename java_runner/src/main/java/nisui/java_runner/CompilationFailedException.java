package nisui.java_runner;

import nisui.core.ExperimentFunctionCreationException;

public class CompilationFailedException extends ExperimentFunctionCreationException {
    private String filename;
    private int exitStatus;
    private String error;

    public CompilationFailedException(String filename, int exitStatus, String error) {
        super(String.format("Compiling %s failed with exit status %s:\n%s", filename, exitStatus, error));
        this.exitStatus = exitStatus;
        this.filename = filename;
        this.error = error;
    }

    public String getFilename() {
        return filename;
    }

    public int getExitStatus() {
        return exitStatus;
    }

    public String getError() {
        return error;
    }
}
