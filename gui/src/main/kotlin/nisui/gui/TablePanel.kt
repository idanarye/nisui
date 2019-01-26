package nisui.gui

import kotlin.reflect.*
import kotlin.reflect.jvm.*

import javax.swing.*
import javax.swing.event.*
import javax.swing.table.*
import javax.swing.border.Border
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.event.KeyEvent
import java.awt.event.ActionEvent

abstract class TablePanel<T>: JScrollPane() {
    val table = JTable()
    val tableModel: AbstractTableModel

    val columns = mutableListOf<Column<T, *>>()
    open fun makeBorder(): Border? = null

    protected abstract fun getRowsSource(): List<T>;
    protected abstract fun addNewEntry(): T;
    protected abstract fun deleteEntry(index: Int);
    protected abstract fun populateColumns();

    init {
        setViewportView(table)
        makeBorder()?.let(::setBorder)
        table.setFillsViewportHeight(true)
        populateColumns()
        setMinimumSize(Dimension(100 * columns.size, 100))
        tableModel = object: AbstractTableModel() {
            override fun getColumnCount(): Int {
                return columns.size
            }
            override fun getColumnName(col: Int): String = columns[col].caption
            override fun getRowCount(): Int = getRowsSource().size + 1
            override fun getValueAt(row: Int, col: Int): Any? {
                val item = getRowsSource().getOrNull(row)
                if (item == null) {
                    return null
                }
                return columns[col].getter(item)
            }

            override fun isCellEditable(row: Int, col: Int) = columns[col].setter != null
            override fun setValueAt(value: Any, row: Int, col: Int) {
                val item = if (row == getRowsSource().size) {
                    val newEntry = addNewEntry()
                    fireTableChanged(TableModelEvent(this, row, row, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
                    newEntry
                } else {
                    getRowsSource()[row]
                }
                columns[col].invokeSetter(item, value)
                fireTableCellUpdated(row, col)
            }

            override fun getColumnClass(col: Int): Class<*> {
                return columns[col].returnType() as Class<*>
            }
        }
        table.setModel(tableModel)

        for ((column, tableColumn) in columns zip table.getColumnModel().getColumns().toList()) {
            column.configTableColumn(tableColumn)
        }

        table.getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete")
        table.getActionMap().put("delete", object: AbstractAction() {
            override fun actionPerformed(e: ActionEvent) {
                val row = table.getSelectedRow()
                if (row < getRowsSource().size) {
                    deleteEntry(row)
                    tableModel.fireTableChanged(TableModelEvent(tableModel, row, row, TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE));
                }
            }
        })
    }
}

class Column<T, V>(val caption: String, val getter: T.() -> V, val setter: (T.(V) -> Unit)? = null) {
    fun invokeSetter(item: T, value: Any?) {
        val setter = setter!!
        item.setter(value as V)
    }

    fun returnType(): Class<V> {
        val getter = getter as KCallable<V>
        return getter.returnType.javaType as Class<V>
    }

    val tableCellEditor: TableCellEditor?

    init {
        if (returnType().isEnum()) {
            tableCellEditor = DefaultCellEditor(JComboBox(returnType().getEnumConstants()))
        } else {
            tableCellEditor = null
        }
    }

    fun configTableColumn(tableColumn: TableColumn) {
        if (tableCellEditor != null) {
            tableColumn.setCellEditor(tableCellEditor)
        }
    }
}
