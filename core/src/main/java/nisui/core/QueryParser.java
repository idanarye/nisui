package nisui.core;


import static org.petitparser.parser.primitive.CharacterParser.*;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import org.petitparser.parser.Parser;
import org.petitparser.parser.combinators.SettableParser;
import org.petitparser.parser.primitive.StringParser;
import org.petitparser.tools.ExpressionBuilder;
import org.petitparser.tools.ExpressionBuilder.ExpressionGroup;

public abstract class QueryParser<V, B> {
    private Parser vParser;
    private Parser bParser;

    protected enum UnariOperator { PLUS, MINUS }
    protected enum BinaryOperator { ADD, SUB, MUL, DIV, POW }
    protected enum ScalarFunction { SQRT, ABS, LOG2, LOG10, LN }
    protected enum ScalarBiFunction { ROOT, LOG }
    protected enum AggregationFunction { COUNT, MAX, MIN, SUM, AVG, SD, COEFF }
    protected enum ComparisonOperator {
        EQ, NE, L, LE, G, GE;

        private static void addParser(ExpressionGroup group, ComparisonOperator op, String... syntax) {
            for (String s : syntax) {
                group.primitive(StringParser.of(s).trim().map((o) -> op));
            }
        }

        static Parser createParser() {
            ExpressionBuilder builder = new ExpressionBuilder();
            ExpressionGroup group = builder.group();
            addParser(group, EQ, "=", "==");
            addParser(group, NE, "!=", "<>");
            addParser(group, LE, "<=");
            addParser(group, L, "<");
            addParser(group, GE, ">=");
            addParser(group, G, ">");
            return builder.build();
        }
    }

    @SuppressWarnings("unchecked")
    protected QueryParser() {
        Parser number = digit().plus().seq(of('.').seq(digit().plus()).optional()).flatten().trim();
        Parser identifier = letter().seq(word().star()).flatten().trim();

        ExpressionBuilder vBuilder = new ExpressionBuilder();
        ExpressionBuilder bBuilder = new ExpressionBuilder();

        SettableParser vTerm = SettableParser.undefined();
        SettableParser bTerm = SettableParser.undefined();

        ExpressionGroup baseGroup = vBuilder.group();
        for (ScalarFunction fn : ScalarFunction.values()) {
            baseGroup.primitive(functionCallParser(fn.toString(), 1, vTerm).map((List<V> values) -> scalarFunction(fn, values.get(2))));
        }
        for (ScalarBiFunction fn : ScalarBiFunction.values()) {
            baseGroup.primitive(functionCallParser(fn.toString(), 2, vTerm).map((List<V> values) -> scalarBiFunction(fn, values.get(2), values.get(4))));
        }
        for (AggregationFunction fn : AggregationFunction.values()) {
            baseGroup.primitive(functionCallParser(fn.toString(), 1, vTerm).map((List<V> values) -> aggregationFunction(fn, values.get(2))));
        }
        baseGroup.primitive(number.map(this::numberLiteral));
        baseGroup.primitive(identifier.map(this::identifier));
        baseGroup.primitive(of('(').trim().seq(vTerm).seq(of(')').trim()).pick(1).map(this::parenthesisValue));

        vBuilder.group()
            .prefix(of('+').trim(), (List<V> values) -> unaryOperator(UnariOperator.PLUS, values.get(1)))
            .prefix(of('-').trim(), (List<V> values) -> unaryOperator(UnariOperator.MINUS, values.get(1)));
        vBuilder.group()
            .right(of('^').trim(), (List<V> values) -> binaryOperator(BinaryOperator.POW, values.get(0), values.get(2)));
        vBuilder.group()
            .left(of('*').trim(), (List<V> values) -> binaryOperator(BinaryOperator.MUL, values.get(0), values.get(2)))
            .left(of('/').trim(), (List<V> values) -> binaryOperator(BinaryOperator.DIV, values.get(0), values.get(2)));
        vBuilder.group()
            .left(of('+').trim(), (List<V> values) -> binaryOperator(BinaryOperator.ADD, values.get(0), values.get(2)))
            .left(of('-').trim(), (List<V> values) -> binaryOperator(BinaryOperator.SUB, values.get(0), values.get(2)));

        bBuilder.group().primitive(
                vTerm.seq(ComparisonOperator.createParser().seq(vTerm).plus())
                .map((List<Object> parts) -> {
                    V firstValue = (V)parts.get(0);
                    List<List<Object>> pairs = (List<List<Object>>)parts.get(1);
                    ArrayList<V> values = new ArrayList<>(pairs.size() + 1);
                    values.add(firstValue);
                    ArrayList<ComparisonOperator> ops = new ArrayList<>(pairs.size());

                    int i = 0;
                    for (List<Object> pair : pairs) {
                        ops.add((ComparisonOperator)pair.get(0));
                        values.add((V)pair.get(1));
                        ++i;
                    }

                    return comparisonChain(values, ops);
                }));

        bBuilder.group().prefix(StringParser.of("NOT").trim(), (List<B> values) -> logicalNot(values.get(1)));
        bBuilder.group().left(StringParser.of("AND").trim(), (List<B> values) -> logicalAnd(values.get(0), values.get(2)));
        bBuilder.group().left(StringParser.of("OR").trim(), (List<B> values) -> logicalOr(values.get(0), values.get(2)));

        vTerm.set(vBuilder.build());
        bTerm.set(bBuilder.build());

        vParser = vTerm.end();
        bParser = bTerm.end();
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

    public V parseValue(String query) {
        return vParser.parse(query).get();
    }

    public B parseBoolean(String query) {
        return bParser.parse(query).get();
    }

    public abstract V numberLiteral(String literal);
    public abstract V identifier(String name);
    public V parenthesisValue(V expr) {
        return expr;
    }
    public abstract V unaryOperator(UnariOperator op, V value);
    public abstract V binaryOperator(BinaryOperator op, V left, V right);
    public abstract V scalarFunction(ScalarFunction fn, V value);
    public abstract V scalarBiFunction(ScalarBiFunction fn, V value1, V value2);
    public abstract V aggregationFunction(AggregationFunction fn, V value);

    public B parenthesisBoolean(B pred) {
        return pred;
    }
    public abstract B comparisonChain(List<V> values, List<ComparisonOperator> ops);
    public abstract B logicalNot(B pred);
    public abstract B logicalAnd(B left, B right);
    public abstract B logicalOr(B left, B right);
}
