package nisui.core;

public abstract class ResultsStorage {
    public abstract void prepareStorage();
    public abstract Connection connect();

    public static abstract class Connection implements AutoCloseable {
    }
}
