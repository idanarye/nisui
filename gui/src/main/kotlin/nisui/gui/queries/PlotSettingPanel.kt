package nisui.gui.queries

import javax.swing.*
import javax.swing.event.*
import java.awt.GridBagLayout
import java.awt.GridBagConstraints
import java.awt.FlowLayout

import nisui.core.plotting.*

import nisui.gui.*

public class PlotSettingPanel(val parent: PlotsPanel): JPanel(GridBagLayout()) {
    var entry = PlotEntry.buildNew("")!!

    init {
        add(PlotsList(this), constraints {
            gridx = 0
            gridy = 0
        })
        add(gridBagJPanel {
            add(JLabel("Name:"))
            val nameField = JTextField()
            nameField.getDocument().addDocumentListener(object: DocumentListener {
                fun update() {
                    entry.setName(nameField.getText())
                    plotUpdated()
                }

                override fun changedUpdate(e: DocumentEvent) {
                    update()
                }
                override fun insertUpdate(e: DocumentEvent) {
                    update()
                }
                override fun removeUpdate(e: DocumentEvent) {
                    update()
                }
            })
            add(nameField)
        }, constraints {
            gridx = 0
            gridy = 1
        })
        add(FiltersTable(this), constraints {
            gridx = 1
            gridy = 0
            gridheight = 2
        })
        add(AxesTable(this), constraints {
            gridx = 0
            gridy = 2
        })
        add(FormulasTable(this), constraints {
            gridx = 1
            gridy = 2
        })
        plotUpdated()
    }

    fun plotUpdated() {
    }
}
