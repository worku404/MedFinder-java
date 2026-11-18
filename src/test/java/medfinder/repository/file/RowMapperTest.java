package medfinder.repository.file;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

/** Local test of the default method RowMapper.mapAll. */
public class RowMapperTest {

    private Map<String, String> row(String id) {
        Map<String, String> row = new HashMap<>();
        if (id != null) {
            row.put("id", id);
        }
        row.put("name", "x");
        return row;
    }

    /** A malformed row is skipped, the good rows are kept, and a warning is printed. */
    @Test
    public void mapAllSkipsMalformedRowsWithWarning() {
        List<Map<String, String>> rows = new ArrayList<>();
        rows.add(row("1"));
        rows.add(row("abc"));
        rows.add(row(null));
        rows.add(row("4"));

        PrintStream original = System.err;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        List<Integer> result;
        try {
            System.setErr(new PrintStream(captured));
            result = new NumberRowMapper().mapAll(rows);
        } finally {
            System.setErr(original);
        }

        assertEquals(2, result.size());
        assertEquals(Integer.valueOf(1), result.get(0));
        assertEquals(Integer.valueOf(4), result.get(1));
        String warnings = captured.toString();
        assertTrue(warnings.contains("data row 2"));
        assertTrue(warnings.contains("data row 3"));
    }

    /** A null list gives an empty list, never null. */
    @Test
    public void mapAllOfNullIsEmpty() {
        assertTrue(new NumberRowMapper().mapAll(null).isEmpty());
    }
}
