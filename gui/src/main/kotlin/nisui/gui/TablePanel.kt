package nisui.gui

import kotlin.reflect.*

import javax.swing.*
import javax.swing.table.*
import java.awt.BorderLayout
import java.awt.Color

/* abstract class TablePanel: JPanel(BorderLayout()) { */
abstract class TablePanel<T>: JScrollPane() {
    val table = JTable()
    val columns = mutableListOf<Column<T, *>>()

    protected abstract fun getRowsSource(): List<T>;
    protected abstract fun populateColumns();

    init {
        /* add(table.getTableHeader(), BorderLayout.NORTH); */
        /* add(table, BorderLayout.CENTER) */
        setViewportView(table)
        table.setFillsViewportHeight(true)
        populateColumns()
        table.setModel(object: AbstractTableModel() {
            override fun getColumnCount(): Int {
                return columns.size
            }
            override fun getColumnName(col: Int): String = columns[col].caption
            override fun getRowCount(): Int = getRowsSource().size
            override fun getValueAt(row: Int, col: Int): Any? {
                val item = getRowsSource()[row]
                return columns[col].getter(item)
            }

            override fun isCellEditable(row: Int, col: Int) = true
            override fun setValueAt(value: Any, row: Int, col: Int) {
                val item = getRowsSource()[row]
                columns[col].invokeSetter(item, value)
                fireTableCellUpdated(row, col)
            }
        })

    }
}

class Column<T, V>(val caption: String, val getter: T.() -> V, val setter: T.(V) -> Unit) {
    fun invokeSetter(item: T, value: Any?) {
        item.setter(value as V)
    }
}
