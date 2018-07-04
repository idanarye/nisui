package nisui.core;

import org.junit.*;

import java.util.Iterator;
import java.util.List;

import org.assertj.core.api.Assertions;

class SimpleStringParser extends QueryParser<String, String> {
    @Override
    public String numberLiteral(String literal) {
        return String.format("NUM[%s]", literal);
    }

    @Override
    public String unaryOperator(UnariOperator op, String value) {
        switch (op) {
            case PLUS: return String.format("PLUS[%s]", value);
            case MINUS: return String.format("MINUS[%s]", value);
        }
        throw new Error("Unknown unary operator " + op);
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
    public String scalarBiFunction(ScalarBiFunction fn, String value1, String value2) {
        return String.format("%s[%s,%s]", fn, value1, value2);
    }

    @Override
    public String aggregationFunction(AggregationFunction fn, String value) {
        return String.format("%s[%s]", fn, value);
    }

    public String comparisonChain(List<String> values, List<ComparisonOperator> ops) {
        StringBuilder builder = new StringBuilder();
        Iterator<String> vIt = values.iterator();
        Iterator<ComparisonOperator> oIt = ops.iterator();

        builder.append(vIt.next());
        while (vIt.hasNext()) {
            builder.append(' ').append(oIt.next()).append(' ');
            builder.append(vIt.next());
        }
        return String.format("CMP[%s]", builder);
    }

    public String logicalNot(String pred) {
        return String.format("NOT[%s]", pred);
    }
    public String logicalAnd(String left, String right) {
        return String.format("AND[%s,%s]", left, right);
    }
    public String logicalOr(String left, String right) {
        return String.format("OR[%s,%s]", left, right);
    }
}

public class QueryParserTest {
    private static void assertParseValue(String parsedFrom, String parsedTo) {
        SimpleStringParser parser = new SimpleStringParser();
        String parsed = parser.parseValue(parsedFrom);
        // System.out.printf("%s -> %s\n", parsedFrom, parsed);
        Assertions.assertThat(parsed).isEqualTo(parsedTo);
    }

    private static void assertParseBoolean(String parsedFrom, String parsedTo) {
        SimpleStringParser parser = new SimpleStringParser();
        String parsed = parser.parseBoolean(parsedFrom);
        // System.out.printf("%s -> %s\n", parsedFrom, parsed);
        Assertions.assertThat(parsed).isEqualTo(parsedTo);
    }

    @Test
    public void testQueries() {
        assertParseValue("12", "NUM[12]");
        assertParseValue("1.2", "NUM[1.2]");
        assertParseValue("-12", "MINUS[NUM[12]]");
        assertParseValue("(12)", "NUM[12]");
        assertParseValue("+(1.2)", "PLUS[NUM[1.2]]");
        assertParseValue("1 + 2 * 3 - 4 / 5 ^ 6", "SUB[ADD[NUM[1],MUL[NUM[2],NUM[3]]],DIV[NUM[4],POW[NUM[5],NUM[6]]]]");
        assertParseValue("1 - 2 - 3", "SUB[SUB[NUM[1],NUM[2]],NUM[3]]");
        assertParseValue("1 - (2 - 3)", "SUB[NUM[1],SUB[NUM[2],NUM[3]]]");
        assertParseValue("sqrt(pi)", "SQRT[IDENT[pi]]");
        assertParseValue("sum(a )", "SUM[IDENT[a]]");
        assertParseValue("RooT(a, b)", "ROOT[IDENT[a],IDENT[b]]");

        assertParseBoolean("1 <= a + b < 2", "CMP[NUM[1] LE ADD[IDENT[a],IDENT[b]] L NUM[2]]");
        assertParseBoolean("NOT 1 >= a AND 2 != b OR c = 3", "OR[AND[NOT[CMP[NUM[1] GE IDENT[a]]],CMP[NUM[2] NE IDENT[b]]],CMP[IDENT[c] EQ NUM[3]]]");
    }
}
