package nisui.cli;

public class ExitException extends RuntimeException {
    public ExitException(String message) {
        super(message);
    }
}
