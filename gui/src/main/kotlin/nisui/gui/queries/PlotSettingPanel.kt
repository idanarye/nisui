package nisui.gui.queries

import javax.swing.*
import javax.swing.event.*
import java.awt.GridBagLayout
import java.awt.GridBagConstraints
import java.awt.FlowLayout
import javax.swing.KeyStroke;
import java.awt.event.ActionEvent;

import nisui.core.plotting.*

import nisui.gui.*

public class PlotSettingPanel(val parent: PlotsPanel): JPanel(GridBagLayout()) {
    fun createResultsStorage() = parent.createResultsStorage()

    var focusedPlotListEntry = PlotListEntry(PlotEntry.buildNew("")!!, PlotListEntry.Status.UNSYNCED)
    val focusedPlot get() = focusedPlotListEntry.plot

    val tablePanels: List<TablePanel<*>>

    val plotsListPanel: PlotsList

    init {
        plotsListPanel = PlotsList(this)

        add(plotsListPanel, constraints {
            gridx = 0
            gridy = 1
        })
        add(gridBagJPanel {
            val button = JButton("SAVE")
            add(button)
            button.setMnemonic('S')
            button.addActionListener({savePlots()})
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

        for (tablePanel in tablePanels) {
            tablePanel.table.getModel().addTableModelListener {
                plotUpdated()
            }
        }
    }

    fun changeFocusedPlot(plotListEntry: PlotListEntry) {
        if (plotListEntry == focusedPlotListEntry) {
            return
        }
        focusedPlotListEntry = plotListEntry
        val oldStatus = plotListEntry.status
        for (tablePanel in tablePanels) {
            tablePanel.tableModel.fireTableDataChanged()
        }
        plotListEntry.status = oldStatus
    }

    fun plotUpdated() {
        if (focusedPlotListEntry.status == PlotListEntry.Status.SYNCED) {
            focusedPlotListEntry.status = PlotListEntry.Status.UNSYNCED
            plotsListPanel.tableModel.fireTableDataChanged()
        }
    }

    fun savePlots() {
        val postActions = mutableListOf<() -> Unit>()
        createResultsStorage().connect().use {con ->
            con.saveStoredPlots().use {saver ->
                for (plot in plotsListPanel.plots) {
                    when (plot.status) {
                        PlotListEntry.Status.UNSYNCED -> {
                            saver.save(plot.plot)
                            postActions.add({plot.status = PlotListEntry.Status.SYNCED})
                        }
                    }
                }
            }
        }
        postActions.forEach({it()})
        plotsListPanel.tableModel.fireTableDataChanged()
    }
}
