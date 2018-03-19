package nisui.core;

import org.junit.*;
import org.assertj.core.api.Assertions;

class SimpleStringParser extends QueryParser<String> {
    @Override
    public String numberLiteral(String literal) {
        return String.format("NUM[%s]", literal);
    }

    @Override
    public String parenthesis(String expr) {
        return expr;
    }

    @Override
    public String binaryOperator(BinaryOperator op, String left, String right) {
        switch (op) {
            case ADD: return String.format("ADD[%s,%s]", left, right);
            case SUB: return String.format("SUB[%s,%s]", left, right);
            case MUL: return String.format("MUL[%s,%s]", left, right);
            case DIV: return String.format("DIV[%s,%s]", left, right);
            case POW: return String.format("POW[%s,%s]", left, right);
        }
        throw new Error("Unknown binary operator " + op);

    }

    @Override
    public String identifier(String name) {
            return String.format("IDENT[%s]", name);
    }

    @Override
    public String scalarFunction(ScalarFunction fn, String value) {
        return String.format("%s[%s]", fn, value);
    }

    @Override
    public String aggregationFunction(AggregationFunction fn, String value) {
        return String.format("%s[%s]", fn, value);
    }
}

public class QueryParserTest {
    private static void assertParse(String parsedFrom, String parsedTo) {
        SimpleStringParser parser = new SimpleStringParser();
        String parsed = parser.parseString(parsedFrom);
        System.out.printf("%s -> %s\n", parsedFrom, parsed);
        Assertions.assertThat(parsed).isEqualTo(parsedTo);
    }

    @Test
    public void testQueries() {
        assertParse("12", "NUM[12]");
        assertParse("(12)", "NUM[12]");
        assertParse("1 + 2 * 3 - 4 / 5 ^ 6", "SUB[ADD[NUM[1],MUL[NUM[2],NUM[3]]],DIV[NUM[4],POW[NUM[5],NUM[6]]]]");
        assertParse("1 - 2 - 3", "SUB[SUB[NUM[1],NUM[2]],NUM[3]]");
        assertParse("1 - (2 - 3)", "SUB[NUM[1],SUB[NUM[2],NUM[3]]]");
        assertParse("sqrt(pi)", "SQRT[IDENT[pi]]");
        assertParse("sum(a)", "SUM[IDENT[a]]");
    }
}
