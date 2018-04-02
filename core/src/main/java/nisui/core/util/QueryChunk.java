package nisui.core.util;

import java.util.function.Consumer;

public class QueryChunk {
    public static final QueryChunk ROOT = new QueryChunk(null);

    public final QueryChunk previous;

    protected QueryChunk(QueryChunk previous) {
        this.previous = previous;
    }

    public QueryChunk sql(String sql) {
        return new QueryChunkSql(this, sql);
    }

    public QueryChunk param(Object param) {
        return new QueryChunkParam(this, param);
    }

    public QueryChunk nest(QueryChunk nested) {
        return new QueryChunkNested(this, nested);
    }

    public void scan(Consumer<String> onSql, Consumer<Object> onParam) {
        if (previous != null) {
            previous.scan(onSql, onParam);
        }
    }
}

class QueryChunkSql extends QueryChunk {
    public final String sql;

    protected QueryChunkSql(QueryChunk previous, String sql) {
        super(previous);
        this.sql = sql;
    }

    public void scan(Consumer<String> onSql, Consumer<Object> onParam) {
        super.scan(onSql, onParam);
        onSql.accept(sql);
    }
}

class QueryChunkParam extends QueryChunk {
    public final Object param;

    protected QueryChunkParam(QueryChunk previous, Object param) {
        super(previous);
        this.param = param;
    }

    public void scan(Consumer<String> onSql, Consumer<Object> onParam) {
        super.scan(onSql, onParam);
        onParam.accept(param);
    }
}

class QueryChunkNested extends QueryChunk {
    public final QueryChunk nested;

    protected QueryChunkNested(QueryChunk previous, QueryChunk nested) {
        super(previous);
        this.nested = nested;
    }

    public void scan(Consumer<String> onSql, Consumer<Object> onParam) {
        super.scan(onSql, onParam);
        nested.scan(onSql, onParam);
    }
}
