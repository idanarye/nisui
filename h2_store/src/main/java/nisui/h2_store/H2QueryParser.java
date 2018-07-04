package nisui.h2_store;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import nisui.core.QueryParser;
import nisui.core.util.QueryChunk;

public class H2QueryParser extends QueryParser<QueryChunk, QueryChunk> {

    @Override
    public QueryChunk numberLiteral(String literal) {
        if (literal.indexOf('.') < 0) {
            return QueryChunk.ROOT.param(Long.parseLong(literal));
        } else {
            return QueryChunk.ROOT.param(Double.parseDouble(literal));
        }
    }

    @Override
    public QueryChunk identifier(String name) {
        return QueryChunk.ROOT.sql(name);
    }

    @Override
    public QueryChunk parenthesisValue(QueryChunk expr) {
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
            case DIV: return QueryChunk.ROOT.sql("(").nest(left).sql(") * 1.0 / (").nest(right).sql(")");
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
            case ROOT: return QueryChunk.ROOT.sql("POWER(").nest(value1).sql(", 1.0 / (").nest(value2).sql("))");
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
                return QueryChunk.ROOT.sql(fn.toString()).sql("(").nest(value).sql(")");
            case AVG:
                return QueryChunk.ROOT.sql("AVG(1.0 * (").nest(value).sql("))");
            case SD:
                return QueryChunk.ROOT.sql("STDDEV_SAMP(").nest(value).sql(")");
            case COEFF:
                return QueryChunk.ROOT.sql("(STDDEV_SAMP(").nest(value).sql(") / AVG(1.0 * (").nest(value).sql(")))");
        }
        throw new Error("Unknown aggregation function " + fn);
    }

    @Override
    public QueryChunk comparisonChain(List<QueryChunk> values, List<ComparisonOperator> ops) {
        QueryChunk result = QueryChunk.ROOT.sql("(");

        Iterator<QueryChunk> vIt = values.iterator();
        Iterator<ComparisonOperator> oIt = ops.iterator();

        QueryChunk prevValue = vIt.next();
        boolean firstIteration = true;

        while (vIt.hasNext()) {
            if (firstIteration) {
                firstIteration = false;
            } else {
                result = result.sql(" AND ");
            }

            QueryChunk value = vIt.next();
            ComparisonOperator op = oIt.next();

            result = result.sql("(").nest(prevValue);
            switch (op) {
                case EQ: result = result.sql(") = ("); break;
                case NE: result = result.sql(") != ("); break;
                case L: result = result.sql(") < ("); break;
                case LE: result = result.sql(") <= ("); break;
                case G: result = result.sql(") > ("); break;
                case GE: result = result.sql(") >= ("); break;
            }
            result = result.nest(value).sql(")");

            prevValue = value;
        }
        return result.sql(")");
    }

    @Override
    public QueryChunk parenthesisBoolean(QueryChunk pred) {
        return QueryChunk.ROOT.sql("(").nest(pred).sql(")");
    }

    @Override
    public QueryChunk logicalNot(QueryChunk pred) {
        return QueryChunk.ROOT.sql("NOT (").nest(pred).sql(")");
    }

    @Override
    public QueryChunk logicalAnd(QueryChunk left, QueryChunk right) {
        return QueryChunk.ROOT.sql("(").nest(left).sql(") AND (").nest(right).sql(")");
    }

    @Override
    public QueryChunk logicalOr(QueryChunk left, QueryChunk right) {
        return QueryChunk.ROOT.sql("(").nest(left).sql(") OR (").nest(right).sql(")");
    }
}
