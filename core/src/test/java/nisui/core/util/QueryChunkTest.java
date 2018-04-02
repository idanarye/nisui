package nisui.core.util;

import org.junit.*;

import java.util.ArrayList;

import org.assertj.core.api.Assertions;

public class QueryChunkTest {
    private void verify(QueryChunk queryChunk, String targetSql, Object... targetParams) {
        StringBuilder sqlBuilder = new StringBuilder();
        ArrayList<Object> params = new ArrayList<>();
        queryChunk.scan(
                sql -> {
                    sqlBuilder.append(sql);
                },
                param -> {
                    sqlBuilder.append("?");
                    params.add(param);
                });
        Assertions.assertThat(sqlBuilder.toString()).isEqualTo(targetSql);
        Assertions.assertThat(params).containsExactly(targetParams);
    }

    @Test
    public void testQuerySimple() {
        verify(QueryChunk.ROOT.sql("SELECT x + ").param(12).sql(" AS y"),
                "SELECT x + ? AS y", 12);
    }

    @Test
    public void testQueryNested() {
        QueryChunk nested = QueryChunk.ROOT.sql("y + ").param(13);
        verify(QueryChunk.ROOT.sql("SELECT x + ").param(12).sql(", ").nest(nested).sql(", ").param(14).sql(" AS z"),
                "SELECT x + ?, y + ?, ? AS z", 12, 13, 14);
    }
}
