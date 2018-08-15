package nisui.core.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

public class ResultSetIterator<T> implements Iterator<T> {
    private ResultSet rs;
    private FunctionThrowsSQLException<T> extractValue;
    private T next;

    public interface FunctionThrowsSQLException<T> {
        public T extractValue(ResultSet rs) throws SQLException;
    }

    public ResultSetIterator(ResultSet rs, FunctionThrowsSQLException<T> extractValue) {
        this.rs = rs;
        this.extractValue = extractValue;
        next = null;
        next();
    }

    @Override
    public boolean hasNext() {
        return next != null;
    }

    @Override
    public T next() {
        try {
            T currentNext = next;
            if (rs.next()) {
                next = extractValue.extractValue(rs);
            } else {
                next = null;
            }
            return currentNext;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
