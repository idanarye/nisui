package nisui.h2_store

import nisui.core.*
import nisui.core.util.*

private fun applyQueryChunk(chunk: QueryChunk, sql: StringBuilder, parameters: MutableList<Any>) {
    chunk.scan({ sql.append(it) }) { param ->
        sql.append(" ? ");
        parameters.add(param);
    }
}

public class H2RunQuery<D, R>(
    con: H2ResultsStorage<D, R>.Connection,
    dataPoints: Iterable<DataPoint<D>>,
    val queries: Array<String>,
    val sortBy: Array<String>
): H2Operations<D, R>(con), QueryRunner<D> {
    val dataPoints: Map<Long, H2DataPoint<D>> = dataPoints.asSequence().map({ it as H2DataPoint }).associateBy({ it.id })
    val sortByIndex = sortBy.mapIndexed { i, sb -> sb to i }.toMap()

    override fun iterator(): MutableIterator<QueryRunner.Row<D>> {
        val parameters = mutableListOf<Any>()
        if (stmt == null) {
            val sql = StringBuilder()
            sql.append("SELECT MIN(data_point_id)")
            val queryParser = H2QueryParser()
            for (query in queries) {
                sql.append(", (")
                applyQueryChunk(queryParser.parseValue(query), sql, parameters)
                sql.append(")")
            }
            sql.append(" FROM ").append(H2ResultsStorage.Connection.EXPERIMENT_RESULTS_TABLE_NAME).append(" AS er")
            sql.append(" INNER JOIN ").append(H2ResultsStorage.Connection.DATA_POINTS_TABLE_NAME).append(" AS dp ON er.data_point_id = dp.id")
            sql.append(" WHERE data_point_id IN (SELECT * FROM TABLE(id BIGINT = ?))")

            sql.append(" GROUP BY ")
            IterWithSeparator.iterWithSep(
                con.parent().dataPointHandler.fields(),
                { sql.append("dp." + it.getName()) },
                { sql.append(", ") }
            )

            sql.append(" ORDER BY ")
            IterWithSeparator.iterWithSep(
                    con.parent().dataPointHandler.fields().asSequence().sortedBy {
                        sortByIndex[it.name] ?: sortByIndex.size
                    }.asIterable(),
                    { sql.append("dp." + it.getName()) },
                    { sql.append(", ") })


            sql.append(';')
            stmt = con.createPreparedStatement(sql.toString())
        }
        for ((i, parameter) in parameters.withIndex()) {
            stmt.setObject(i + 1, parameter)
        }
        val iter = dataPoints.keys.iterator()
        val array = stmt.getConnection().createArrayOf("BIGINT", Array(dataPoints.size, { iter.next() }))
        stmt.setArray(parameters.size + 1, array)
        return ResultSetIterator(stmt.executeQuery()) { rs ->
            val values = DoubleArray(queries.size, { rs.getDouble(it + 2) })
            QueryRunner.Row<D>(dataPoints[rs.getLong(1)]!!.getValue(), values)
        }
    }
}
