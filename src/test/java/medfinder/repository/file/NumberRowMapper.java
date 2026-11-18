package medfinder.repository.file;

import java.util.HashMap;
import java.util.Map;

/** Test helper: maps rows with columns "id" and "name" to the id number. */
public class NumberRowMapper implements RowMapper<Integer> {

    @Override
    public String[] headers() {
        return new String[] {"id", "name"};
    }

    @Override
    public Integer fromRow(Map<String, String> row) {
        return Integer.valueOf(row.get("id"));
    }

    @Override
    public Map<String, String> toRow(Integer entity) {
        Map<String, String> row = new HashMap<>();
        row.put("id", String.valueOf(entity));
        row.put("name", "n" + entity);
        return row;
    }
}
