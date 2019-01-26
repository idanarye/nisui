package nisui.jzy3d_plots

import java.awt.Component

import org.jzy3d.chart2d.Chart2d
import org.jzy3d.chart.Chart
import org.jzy3d.chart.ChartLauncher
import org.jzy3d.plot2d.primitives.Serie2d
import org.jzy3d.colors.Color

import nisui.core.*
import nisui.core.plotting.*
import nisui.core.util.ExperimentValueQueryEvaluator

class Jzy3d2dPlot(val nisuiFactory: NisuiFactory) {
    val chart = Chart2d()

    val canvas = chart.getCanvas() as Component

    fun render(plotEntry: PlotEntry) {
        val dpHandler = nisuiFactory.createExperimentFunction().dataPointHandler
        val quaryEvaluator = ExperimentValueQueryEvaluator(dpHandler)
        val axesEvaluators = plotEntry.axes.map{ quaryEvaluator.parseValue(it.expression) }

        val formulasSerie = plotEntry.formulas.mapIndexed {i, it ->
            val serie = chart.getSerie(it.textForPlot, Serie2d.Type.LINE)
            serie.setColor(Color.COLORS[i % Color.COLORS.size]) // TODO: set color dynamically
            serie
        }

        val axeLayout = chart.getAxeLayout()
        axeLayout.setXAxeLabel(plotEntry.axes[0].textForPlot)

        nisuiFactory.createResultsStorage().connect().use {con ->
            val dataPoints = con.readDataPoints(plotEntry.filters.map {it.expression}).use {
                it.toList()
            }
            con.runQuery(
                    dataPoints,
                    plotEntry.formulas.map {it.expression},
                    plotEntry.axes.map {it.expression}
            ).use { queryRunner ->
                for (row in queryRunner) {
                    val row = row as nisui.core.QueryRunner.Row<*>
                    val axes = axesEvaluators.map { it(row.dataPoint) }
                    for ((serie, value) in formulasSerie.zip(row.values.toList())) {
                        serie.add(axes[0], value)
                    }
                }
            }
        }

        canvas.repaint()
        // println("Repainted ${System.identityHashCode(canvas)}")
        // ChartLauncher.openChart(chart)
    }
}
