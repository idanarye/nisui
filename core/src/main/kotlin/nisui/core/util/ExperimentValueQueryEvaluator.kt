package nisui.core.util

import kotlin.math.*

import nisui.core.*

public class ExperimentValueQueryEvaluator<V>(val valueHandler: ExperimentValuesHandler<V>) : QueryParser<(V)->Double, (V)->Boolean>() {
    protected override fun numberLiteral(literal: String): (V)->Double = {literal.toDouble()}
    protected override fun identifier(name: String): (V)->Double {
       val field = valueHandler.field(name)
       return {(field.get(it) as Number).toDouble()}
    }
    protected override fun unaryOperator(op: UnariOperator, value: (V)->Double): (V)->Double {
       return when(op) {
          UnariOperator.PLUS -> value
          UnariOperator.MINUS -> ({-value(it)})
       }
    }
    protected override fun binaryOperator(op: BinaryOperator, left: (V)->Double, right: (V)->Double): (V)->Double {
       return when(op) {
          BinaryOperator.ADD -> ({ left(it) + right(it) })
          BinaryOperator.SUB -> ({ left(it) - right(it) })
          BinaryOperator.MUL -> ({ left(it) * right(it) })
          BinaryOperator.DIV -> ({ left(it) / right(it) })
          BinaryOperator.POW -> ({ left(it).pow(right(it)) })
       }
    }
    protected override fun scalarFunction(fn: ScalarFunction, value: (V)->Double): (V)->Double {
       return when(fn) {
          ScalarFunction.SQRT -> ({ sqrt(value(it)) })
          ScalarFunction.ABS -> ({ abs(value(it)) })
          ScalarFunction.LOG2 -> ({ log2(value(it)) })
          ScalarFunction.LOG10 -> ({ log10(value(it)) })
          ScalarFunction.LN -> ({ ln(value(it)) })
       }
    }
    protected override fun scalarBiFunction(fn: ScalarBiFunction, value1: (V)->Double, value2: (V)->Double): (V)->Double {
       return when(fn) {
          ScalarBiFunction.ROOT -> ({ value1(it).pow(1 / value2(it)) })
          ScalarBiFunction.LOG -> ({ log(value1(it), value2(it)) })
       }
    }

    protected override fun aggregationFunction(fn: AggregationFunction, value: (V)->Double): (V)->Double {
       throw Error("Aggregation functions not supported here")
    }

    protected override fun comparisonChain(values: List<(V)->Double>, ops: List<ComparisonOperator>): (V)->Boolean {
       var pred = {v: V -> true}
       for ((pair, op) in values.zipWithNext().zip(ops)) {
          val (left, right) = pair
          val oldPred = pred
          pred = when(op) {
             ComparisonOperator.EQ -> ({ oldPred(it) && left(it) == right(it) })
             ComparisonOperator.NE -> ({ oldPred(it) && left(it) != right(it) })
             ComparisonOperator.L -> ({ oldPred(it) && left(it) < right(it) })
             ComparisonOperator.LE -> ({ oldPred(it) && left(it) <= right(it) })
             ComparisonOperator.G -> ({ oldPred(it) && left(it) > right(it) })
             ComparisonOperator.GE -> ({ oldPred(it) && left(it) >= right(it) })
          }
       }
       return pred
    }
    protected override fun logicalNot(pred: (V)->Boolean): (V)->Boolean = { !pred(it) }
    protected override fun logicalAnd(left: (V)->Boolean, right: (V)->Boolean): (V)->Boolean = { left(it) && right(it) }
    protected override fun logicalOr(left: (V)->Boolean, right: (V)->Boolean): (V)->Boolean = { left(it) || right(it) }
}
