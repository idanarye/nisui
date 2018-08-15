package nisui.h2_store;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Function;

import nisui.core.StoredPlotSaver;
import nisui.core.plotting.PlotEntry;

public class H2SaveStoredPlot<D, R> extends H2Operations<D, R> implements StoredPlotSaver {
	H2SaveStoredPlot(H2ResultsStorage<D, R>.Connection con) {
		super(con);
		StringBuilder sql = new StringBuilder();
		sql.append("MERGE INTO ").append(con.STORED_PLOTS_TABLE_NAME).append("(");
		sql.append("id, name, axes, filters, formulas");
		sql.append(") VALUES(");
		sql.append("?, ?, ?, ?, ?");
		sql.append(");");
		stmt = con.createPreparedStatement(sql.toString());
	}

	private static <T> Object[] toArray(List<T> items, Function<T, Object[]> mapper) {
		return items.stream().map(mapper).toArray(Object[]::new);
	}

	@Override
	public void save(PlotEntry plotEntry) throws SQLException {
		stmt.clearParameters();
		stmt.setString(1, plotEntry.getKey());
		stmt.setString(2, plotEntry.getName());
		stmt.setObject(3, toArray(plotEntry.getAxes(), axis ->
					new Object[]{axis.getCaption(), axis.getScaleType().toString(), axis.getUnitName(), axis.getExpression()}));
		stmt.setObject(4, toArray(plotEntry.getFilters(), filter ->
					new Object[]{filter.getCaption(), filter.getFilterType().toString(), filter.getUnitName(), filter.getExpression()}));
		stmt.setObject(5, toArray(plotEntry.getFormulas(), formula ->
					new Object[]{formula.getCaption(), formula.getSymbol(), formula.getScaleType().toString(), formula.getUnitName(), formula.getExpression()}));
		stmt.executeUpdate();
		ResultSet generatedKeys = stmt.getGeneratedKeys();
		if (generatedKeys.next()) {
			plotEntry.setKey(generatedKeys.getString(1));
		}
	}
}
