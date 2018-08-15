package nisui.h2_store;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Collectors;

import nisui.core.StoredPlotsReader;
import nisui.core.plotting.*;
import nisui.core.util.ResultSetIterator;

public class H2ReadStoredPlots<D, R> extends H2Operations<D, R> implements StoredPlotsReader {
    H2ReadStoredPlots(H2ResultsStorage<D, R>.Connection con) {
        super(con);
    }

    @Override
    public Iterator<PlotEntry> iterator() {
        if (stmt == null) {
            stmt = con.createPreparedStatement(String.format(
                        "SELECT id, name, axes, filters, formulas FROM %s",
                        con.STORED_PLOTS_TABLE_NAME));
        }
        try {
            return new ResultSetIterator<>(stmt.executeQuery(), rs -> {
                String key = rs.getString(1);
                String caption = rs.getString(2);

                List<PlotAxis> axes = Arrays.stream((Object[])rs.getObject(3)).map(Object[].class::cast)
                    .map(axis -> new PlotAxis((String)axis[0], ScaleType.valueOf((String)axis[1]), (String)axis[2], (String)axis[3]))
                    .collect(Collectors.toList());

                List<PlotFilter> filters = Arrays.stream((Object[])rs.getObject(4)).map(Object[].class::cast)
                    .map(filter -> new PlotFilter((String)filter[0], FilterType.valueOf((String)filter[1]), (String)filter[2], (String)filter[3]))
                    .collect(Collectors.toList());

                List<PlotFormula> formulas = Arrays.stream((Object[])rs.getObject(5)).map(Object[].class::cast)
                    .map(formula -> new PlotFormula((String)formula[0], (String)formula[1], ScaleType.valueOf((String)formula[2]), (String)formula[3], (String)formula[4]))
                    .collect(Collectors.toList());

                return new PlotEntry(key, caption, axes, filters, formulas);
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
