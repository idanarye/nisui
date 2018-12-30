package nisui.gui

import kotlin.reflect.*

import javax.swing.*
import java.awt.GridBagLayout
import java.awt.GridBagConstraints
import java.awt.Color

abstract class TablePanel<Row: Any>(val cls: KClass<Row>): JPanel(GridBagLayout()) {
    val columns = cls.members.asSequence().map {member ->
        if (member is KCallable<*>) {
            (member.annotations.find({it is Column}))?.let {ann ->
                ann as Column to member as KCallable<JComponent>
            }
        } else {
            null
        }
    }.filterNotNull().withIndex().map {(index, item) ->
        val (ann, field) = item
        Triple(index, ann, field)
    }.toList()

    val rows = mutableListOf<Row>()

    val addRowButton = JButton("+")

    init {
        for ((index, ann, _) in columns) {
            val label = JLabel(ann.caption)
            add(label, constraints {
                gridx = index
                gridy = 0
            })
            label.setBorder(BorderFactory.createLineBorder(Color.black))
        }
        add(addRowButton, constraints {
            gridx = 0
            /* gridwidth = columns.size */
            gridy = 1
        })
        addRowButton.addActionListener {
            addRow(createNewRow())
        }
    }

    fun addRow(row: Row) {
        rows.add(row)
        for ((index, _, field) in columns) {
            val field = field.call(row)
            add(field, constraints {
                gridx = index
                gridy = rows.size
                println("$gridx $gridy")
            })
        }
        repaint()
    }

    abstract fun createNewRow(): Row
}

annotation class Column(val caption: String) {
}
