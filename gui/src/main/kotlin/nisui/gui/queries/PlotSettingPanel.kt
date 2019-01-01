package nisui.gui.queries

import javax.swing.*
import javax.swing.event.*
import java.awt.GridBagLayout
import java.awt.GridBagConstraints
import java.awt.FlowLayout

import nisui.core.plotting.*

import nisui.gui.*

public class PlotSettingPanel(val parent: PlotsPanel): JPanel(GridBagLayout()) {
    var focusedPlot = PlotEntry.buildNew("")!!

    val tablePanels: List<TablePanel<*>>

    init {
        add(PlotsList(this), constraints {
            gridx = 0
            gridy = 1
        })
        add(gridBagJPanel {
            add(JButton("SAVE"))
        }, constraints {
            gridx = 0
            gridy = 0
        })
        val tablePanels = mutableListOf<TablePanel<*>>()
        fun addTablePanel(tablePanel: TablePanel<*>): TablePanel<*> {
            tablePanels.add(tablePanel)
            return tablePanel
        }

        add(addTablePanel(FiltersTable(this)), constraints {
            gridx = 1
            gridy = 0
            gridheight = 2
        })
        add(addTablePanel(AxesTable(this)), constraints {
            gridx = 0
            gridy = 2
        })
        add(addTablePanel(FormulasTable(this)), constraints {
            gridx = 1
            gridy = 2
        })

        this.tablePanels = tablePanels

        plotUpdated()
    }

    fun changeFocusedPlot(plot: PlotEntry) {
        if (plot == focusedPlot) {
            return
        }
        focusedPlot = plot
        for (tablePanel in tablePanels) {
            tablePanel.tableModel.fireTableDataChanged()
        }
    }

    fun plotUpdated() {
    }
}
