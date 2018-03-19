package nisui.core;


import static org.petitparser.parser.primitive.CharacterParser.*;

import java.util.List;
import org.petitparser.parser.Parser;
import org.petitparser.parser.combinators.SettableParser;
import org.petitparser.tools.ExpressionBuilder;

public abstract class QueryParser<T> {
    private Parser parser;

    protected enum BinaryOperator { ADD, SUB, MUL, DIV, POW }
    protected enum ScalarFunction { SQRT }
    protected enum AggregationFunction { COUNT, MAX, MIN, SUM, AVG, SD, COEFF }

    protected QueryParser() {
        Parser number = digit().plus().flatten().trim();
        Parser identifier = letter().seq(word().star()).flatten().trim();

        ExpressionBuilder builder = new ExpressionBuilder();

        SettableParser term = SettableParser.undefined();

        builder.group()
            .primitive(number.map(this::numberLiteral))
            .primitive(identifier.trim().seq(of('(').trim()).seq(term).seq(of(')').trim()).map((List<Object> values) -> {
                String fnName = (String)values.get(0);
                String fnUppercase = fnName.toUpperCase();
                @SuppressWarnings("unchecked")
                T fnArg = (T)values.get(2);

                ScalarFunction scalarFn;
                try {
                    scalarFn = ScalarFunction.valueOf(fnUppercase);
                } catch (IllegalArgumentException e) {
                    scalarFn = null;
                }
                if (scalarFn != null) {
                    return scalarFunction(scalarFn, fnArg);
                }

                AggregationFunction aggregationFn;
                try {
                    aggregationFn = AggregationFunction.valueOf(fnUppercase);
                } catch (IllegalArgumentException e) {
                    aggregationFn = null;
                }
                if (aggregationFn != null) {
                    return aggregationFunction(aggregationFn, fnArg);
                }

                return null; // TODO: thrown an actual parse exception?
            }))
            .primitive(identifier.map(this::identifier))
            .primitive(of('(').trim().seq(term).seq(of(')').trim()).pick(1).map(this::parenthesis));
        builder.group()
            .right(of('^').trim(), (List<T> values) -> binaryOperator(BinaryOperator.POW, values.get(0), values.get(2)));
        builder.group()
            .left(of('*').trim(), (List<T> values) -> binaryOperator(BinaryOperator.MUL, values.get(0), values.get(2)))
            .left(of('/').trim(), (List<T> values) -> binaryOperator(BinaryOperator.DIV, values.get(0), values.get(2)));
        builder.group()
            .left(of('+').trim(), (List<T> values) -> binaryOperator(BinaryOperator.ADD, values.get(0), values.get(2)))
            .left(of('-').trim(), (List<T> values) -> binaryOperator(BinaryOperator.SUB, values.get(0), values.get(2)));

        term.set(builder.build());

        parser = term.end();
    }

    public T parseString(String query) {
        return parser.parse(query).get();
    }

    public abstract T numberLiteral(String literal);
    public abstract T identifier(String name);
    public abstract T parenthesis(T expr);
    public abstract T binaryOperator(BinaryOperator op, T left, T right);
    public abstract T scalarFunction(ScalarFunction fn, T value);
    public abstract T aggregationFunction(AggregationFunction fn, T value);
}
