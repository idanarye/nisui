package nisui.core.util

import org.junit.Test
import org.assertj.core.api.Assertions.assertThat

import nisui.core.DynamicExperimentValueHandler

class ExperimentValueQueryEvaluatorTest {
    @Test
    fun testBasicParsing() {
        val handler = DynamicExperimentValueHandler().let {
            it.addField("x", Int::class.java)
            it.addField("y", Int::class.java)
            it.addField("e", Double::class.java)
            it
        }

        val v1 = handler.createValue()
        v1.set("x", 5)
        v1.set("y", 8)
        v1.set("e", kotlin.math.E)

        val evaluatorCreator = ExperimentValueQueryEvaluator(handler)
        fun check(expr: String, value: Number) {
            assertThat(evaluatorCreator.parseValue(expr)(v1)).isEqualTo(value.toDouble())
        }
        fun check(expr: String, value: Boolean) {
            assertThat(evaluatorCreator.parseBoolean(expr)(v1)).isEqualTo(value)
        }
        check("10 * x + y / 2", 10 * 5 + 8 / 2)

        check("SQRT(2 * y)", 4)
        check("ABS(x - y)", 3)
        check("LOG2(y)", 3)
        check("LOG10(x * 2)", 1)
        check("LN(e ^ 10)", 10)

        check("ROOT(32, x)", 2)
        check("LOG(64, y)", 2)

        check("1 < 2 < 3", true)
        check("1 < 2 < 2", false)
        check("1 < 2 <= 2", true)
        check("3 < 2 <= 2", false)
        check("3 <= 2 <= 2", false)
        check("2 <= 2 <= 2", true)
        check("1 > 2", false)
        check("1 = 2", false)
        check("1 != 2", true)
        check("1 >= 2", false)
        check("2 >= 2", true)

        check("NOT 1 < 2", false)
        check("NOT 2 < 1", true)
        check("1 < 2 AND 2 < 3", true)
        check("1 < 2 AND 2 = 3", false)

        check("1 < 2 OR 2 < 3", true)
        check("1 < 2 OR 2 = 3", true)
        check("1 < 0 OR 2 = 3", false)
    }
}
