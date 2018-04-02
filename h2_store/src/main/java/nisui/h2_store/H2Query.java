package nisui.h2_store;

import java.util.LinkedList;
import java.util.List;

import nisui.core.util.QueryChunk;

public class H2Query<D> {
    public String sql;
    public List<Object> params;

    H2Query(QueryChunk queryChunk) {
        StringBuilder sqlBuilder = new StringBuilder();

        sqlBuilder.append("SELECT data_point_id, (");

        LinkedList<Object> parameters = new LinkedList<>();
        queryChunk.scan(sql -> {
            sqlBuilder.append(sql);
        }, param -> {
            parameters.add(param);
        });

        sqlBuilder.append(") FROM ");
        sqlBuilder.append(H2ResultsStorage.Connection.EXPERIMENT_RESULTS_TABLE_NAME);

        this.sql = sqlBuilder.toString();
    }
}
