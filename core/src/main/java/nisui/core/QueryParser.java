package nisui.core;


import static org.petitparser.parser.primitive.CharacterParser.*;

import java.util.List;
import org.petitparser.parser.Parser;
import org.petitparser.parser.combinators.SettableParser;
import org.petitparser.parser.primitive.StringParser;
import org.petitparser.tools.ExpressionBuilder;
import org.petitparser.tools.ExpressionBuilder.ExpressionGroup;

public abstract class QueryParser<T> {
    private Parser parser;

    protected enum UnariOperator { PLUS, MINUS }
    protected enum BinaryOperator { ADD, SUB, MUL, DIV, POW }
    protected enum ScalarFunction { SQRT, ABS, LOG2, LOG10, LN }
    protected enum ScalarBiFunction { ROOT, LOG }
    protected enum AggregationFunction { COUNT, MAX, MIN, SUM, AVG, SD, COEFF }

    protected QueryParser() {
        Parser number = digit().plus().seq(of('.').seq(digit().plus()).optional()).flatten().trim();
        Parser identifier = letter().seq(word().star()).flatten().trim();

        ExpressionBuilder builder = new ExpressionBuilder();

        SettableParser term = SettableParser.undefined();

        ExpressionGroup baseGroup = builder.group();
        for (ScalarFunction fn : ScalarFunction.values()) {
            baseGroup.primitive(functionCallParser(fn.toString(), 1, term).map((List<T> values) -> scalarFunction(fn, values.get(2))));
        }
        for (ScalarBiFunction fn : ScalarBiFunction.values()) {
            baseGroup.primitive(functionCallParser(fn.toString(), 2, term).map((List<T> values) -> scalarBiFunction(fn, values.get(2), values.get(4))));
        }
        for (AggregationFunction fn : AggregationFunction.values()) {
            baseGroup.primitive(functionCallParser(fn.toString(), 1, term).map((List<T> values) -> aggregationFunction(fn, values.get(2))));
        }
        baseGroup.primitive(number.map(this::numberLiteral));
        baseGroup.primitive(identifier.map(this::identifier));
        baseGroup.primitive(of('(').trim().seq(term).seq(of(')').trim()).pick(1).map(this::parenthesis));

        builder.group()
            .prefix(of('+').trim(), (List<T> values) -> unaryOperator(UnariOperator.PLUS, values.get(1)))
            .prefix(of('-').trim(), (List<T> values) -> unaryOperator(UnariOperator.MINUS, values.get(1)));
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

    private static Parser functionCallParser(String name, int arity, SettableParser term) {
        Parser result = StringParser.ofIgnoringCase(name).trim();
        result = result.seq(of('(').trim());
        for (int i = 0; i < arity; ++i) {
            if (0 < i) {
                result = result.seq(of(',').trim());
            }
            result = result.seq(term);
        }
        result = result.seq(of(')').trim());
        return result;
    }

    public T parseString(String query) {
        return parser.parse(query).get();
    }

    public abstract T numberLiteral(String literal);
    public abstract T identifier(String name);
    public abstract T parenthesis(T expr);
    public abstract T unaryOperator(UnariOperator op, T value);
    public abstract T binaryOperator(BinaryOperator op, T left, T right);
    public abstract T scalarFunction(ScalarFunction fn, T value);
    public abstract T scalarBiFunction(ScalarBiFunction fn, T value1, T value2);
    public abstract T aggregationFunction(AggregationFunction fn, T value);
}
