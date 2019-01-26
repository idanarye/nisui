package nisui.gui.queries

import javax.swing.*
import java.awt.BorderLayout

import nisui.core.plotting.*

import nisui.jzy3d_plots.Jzy3d2dPlot

import nisui.gui.*

public class PlotViewPanel(val parent: PlotsPanel): JPanel(BorderLayout()) {
    fun renderPlot(plotEntry: PlotEntry) {
        // TODO: reuse plot canvas
        val plot = Jzy3d2dPlot(parent.nisuiFactory)
        removeAll()
        add(plot.canvas)
        plot.render(plotEntry)
        revalidate()
        repaint()
    }
}
