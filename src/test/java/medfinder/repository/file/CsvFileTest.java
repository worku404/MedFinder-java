package medfinder.repository.file;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Local tests of CsvFile (not presented to the instructor). */
public class CsvFileTest {

    @TempDir
    Path folder;

    private final CsvFile csv = new CsvFile();

    private void writeText(Path file, String text) throws IOException {
        Files.write(file, text.getBytes(StandardCharsets.UTF_8));
    }

    /** Commas, quotes, a non-ASCII letter and an empty value survive write then read. */
    @Test
    public void roundTripKeepsCommasQuotesAndUnicode() throws IOException {
        Path file = folder.resolve("a.csv");
        String[] headers = {"id", "name", "note"};
        Map<String, String> row = new HashMap<>();
        row.put("id", "1");
        row.put("name", "Abebe, \"Kebede\" Pharmacy");
        row.put("note", "");
        Map<String, String> row2 = new HashMap<>();
        row2.put("id", "2");
        row2.put("name", "\u1230\u120b\u121d");
        row2.put("note", "plain");
        List<Map<String, String>> rows = new ArrayList<>();
        rows.add(row);
        rows.add(row2);

        csv.write(file, headers, rows);
        List<Map<String, String>> back = csv.read(file);

        assertEquals(2, back.size());
        assertEquals("Abebe, \"Kebede\" Pharmacy", back.get(0).get("name"));
        assertEquals("", back.get(0).get("note"));
        assertEquals("\u1230\u120b\u121d", back.get(1).get("name"));
        assertEquals("plain", back.get(1).get("note"));
    }

    /** Columns are read by name, so a different column order gives the same values. */
    @Test
    public void readsByHeaderNameInAnyOrder() throws IOException {
        Path file = folder.resolve("b.csv");
        writeText(file, "name,id\nBole Pharmacy,7\n");

        List<Map<String, String>> rows = csv.read(file);

        assertEquals(1, rows.size());
        assertEquals("7", rows.get(0).get("id"));
        assertEquals("Bole Pharmacy", rows.get(0).get("name"));
    }

    /** A row with the wrong field count or an open quote is skipped with a warning. */
    @Test
    public void malformedRowIsSkippedWithWarning() throws IOException {
        Path file = folder.resolve("c.csv");
        writeText(file, "id,name\n1,Good\n2\n3,\"open quote\n4,Also good\n\n");

        PrintStream original = System.err;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        List<Map<String, String>> rows;
        try {
            System.setErr(new PrintStream(captured));
            rows = csv.read(file);
        } finally {
            System.setErr(original);
        }

        assertEquals(2, rows.size());
        assertEquals("Good", rows.get(0).get("name"));
        assertEquals("Also good", rows.get(1).get("name"));
        String warnings = captured.toString();
        assertTrue(warnings.contains("line 3"));
        assertTrue(warnings.contains("line 4"));
        assertTrue(warnings.contains("c.csv"));
    }

    /** A byte order mark (added by some editors) does not spoil the first header. */
    @Test
    public void byteOrderMarkIsIgnored() throws IOException {
        Path file = folder.resolve("d.csv");
        writeText(file, "\uFEFFid,name\n1,A\n");

        assertEquals("1", csv.read(file).get(0).get("id"));
    }

    /** A missing file is an IOException that names the file. */
    @Test
    public void missingFileThrowsIoExceptionNamingFile() {
        try {
            csv.read(folder.resolve("nothing.csv"));
            fail("Expected IOException");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("nothing.csv"));
        }
    }

    /** Writing replaces the old content and leaves no temporary file behind. */
    @Test
    public void writeReplacesFileAndLeavesNoTempFile() throws IOException {
        Path file = folder.resolve("e.csv");
        writeText(file, "id,name\n1,Old\n");
        String[] headers = {"id", "name"};
        Map<String, String> row = new HashMap<>();
        row.put("id", "9");
        row.put("name", "New");
        List<Map<String, String>> rows = new ArrayList<>();
        rows.add(row);

        csv.write(file, headers, rows);

        List<Map<String, String>> back = csv.read(file);
        assertEquals(1, back.size());
        assertEquals("New", back.get(0).get("name"));
        int files = 0;
        for (Path p : Files.newDirectoryStream(folder)) {
            files++;
            assertFalse(p.getFileName().toString().endsWith(".tmp"));
        }
        assertEquals(1, files);
    }

    /** write creates the parent folder when it does not exist yet. */
    @Test
    public void writeCreatesMissingFolder() throws IOException {
        Path file = folder.resolve("sub").resolve("f.csv");
        String[] headers = {"id"};
        Map<String, String> row = new HashMap<>();
        row.put("id", "1");
        List<Map<String, String>> rows = new ArrayList<>();
        rows.add(row);

        csv.write(file, headers, rows);

        assertTrue(Files.isRegularFile(file));
    }
}
