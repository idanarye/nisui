package nisui.h2_store;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedList;

import nisui.core.QueryParser;
import nisui.core.util.QueryChunk;

public class H2QueryParser extends QueryParser<QueryChunk> {

    @Override
    public QueryChunk numberLiteral(String literal) {
        return QueryChunk.ROOT.sql(literal);
    }

    @Override
    public QueryChunk identifier(String name) {
        return QueryChunk.ROOT.sql(name);
    }

    @Override
    public QueryChunk parenthesis(QueryChunk expr) {
        return QueryChunk.ROOT.sql("(").nest(expr).sql(")");
    }

    @Override
    public QueryChunk unaryOperator(UnariOperator op, QueryChunk value) {
        String opSql;
        switch (op) {
            case PLUS: return QueryChunk.ROOT.sql("+(").nest(value).sql(")");
            case MINUS: return QueryChunk.ROOT.sql("-(").nest(value).sql(")");
        }
        throw new Error("Unknown unary operator " + op);
    }

    @Override
    public QueryChunk binaryOperator(BinaryOperator op, QueryChunk left, QueryChunk right) {
        String opSql;
        switch (op) {
            case ADD: return QueryChunk.ROOT.sql("(").nest(left).sql(") + (").nest(right).sql(")");
            case SUB: return QueryChunk.ROOT.sql("(").nest(left).sql(") - (").nest(right).sql(")");
            case MUL: return QueryChunk.ROOT.sql("(").nest(left).sql(") * (").nest(right).sql(")");
            case DIV: return QueryChunk.ROOT.sql("(").nest(left).sql(") / (").nest(right).sql(")");
            case POW: return QueryChunk.ROOT.sql("POWER(").nest(left).sql(", ").nest(right).sql(")");
        }
        throw new Error("Unknown binary operator " + op);
    }

    @Override
    public QueryChunk scalarFunction(ScalarFunction fn, QueryChunk value) {
        switch (fn) {
            case SQRT:
            case ABS:
            case LOG2:
            case LOG10:
            case LN:
                return QueryChunk.ROOT.sql(fn.toString()).sql("(").nest(value).sql(")");
        }
        throw new Error("Unknown scalar function " + fn);
    }

    @Override
    public QueryChunk scalarBiFunction(ScalarBiFunction fn, QueryChunk value1, QueryChunk value2) {
        switch (fn) {
            case ROOT: return QueryChunk.ROOT.sql("POWER(").nest(value1).sql(", 1 / (").nest(value2).sql("))");
            case LOG: return QueryChunk.ROOT.sql("(LOG(").nest(value1).sql(") / LOG(").nest(value2).sql("))");
        }
        throw new Error("Unknown scalar bi-function " + fn);
    }

    @Override
    public QueryChunk aggregationFunction(AggregationFunction fn, QueryChunk value) {
        switch (fn) {
            case COUNT:
            case MAX:
            case MIN:
            case SUM:
            case AVG:
                return QueryChunk.ROOT.sql(fn.toString()).sql("(").nest(value).sql(")");
            case SD:
                return QueryChunk.ROOT.sql("STDDEV_SAMP(").nest(value).sql(")");
            case COEFF:
                return QueryChunk.ROOT.sql("(STDDEV_SAMP(").nest(value).sql(") / AVG(").nest(value).sql("))");
        }
        throw new Error("Unknown aggregation function " + fn);
    }
}
