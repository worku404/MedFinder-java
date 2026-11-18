package medfinder.repository.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads and writes UTF-8 CSV files with a header line.
 *
 * <p>Rows are read <b>by column name</b>, so adding or reordering a column does
 * not break existing code. Fields containing a comma or a quote are wrapped in
 * quotes, and a quote inside a field is doubled ({@code ""}).
 *
 * <p>This class does not translate errors: every method declares
 * {@code throws IOException}. Each repository catches it and throws
 * {@code DataStorageException(message, cause)}.
 */
public class CsvFile {

    private static final char SEPARATOR = ',';
    private static final char QUOTE = '"';
    private static final char BYTE_ORDER_MARK = '\uFEFF';

    /**
     * Reads a whole CSV file into rows keyed by header name.
     *
     * <p>A blank line is ignored. A row with the wrong number of fields or an
     * unclosed quote is skipped and a warning naming the file and line is
     * printed to {@code System.err} (FR-DAT-03).
     *
     * @param path the file to read
     * @return one map per good row (column name to text); empty if the file has only a header
     * @throws IOException if the file is missing, empty (no header) or cannot be read
     */
    public List<Map<String, String>> read(Path path) throws IOException {
        if (!Files.isRegularFile(path)) {
            throw new IOException("Data file not found: " + path);
        }
        List<Map<String, String>> rows = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IOException("Data file is empty (no header line): " + path);
            }
            List<String> headers = parseLine(stripByteOrderMark(headerLine));
            if (headers == null) {
                throw new IOException("Data file has a bad header line: " + path);
            }
            readDataLines(reader, headers, path, rows);
        }
        return rows;
    }

    /**
     * Writes rows safely: the data goes to a temporary file in the same folder,
     * which then replaces the original in one step, so a failed write never
     * leaves a half-written file (FR-DAT-04).
     *
     * <p>A missing key in a row is written as an empty field. A line break inside
     * a value is replaced by a space, because each row is one line.
     *
     * @param path the file to create or replace
     * @param headers the column names, in the order to write them
     * @param rows the rows to write (column name to text)
     * @throws IOException if the file cannot be written
     */
    public void write(Path path, String[] headers, List<Map<String, String>> rows) throws IOException {
        Path target = path.toAbsolutePath();
        Path folder = target.getParent();
        Files.createDirectories(folder);
        Path temp = Files.createTempFile(folder, "medfinder-", ".tmp");
        try {
            writeTemp(temp, headers, rows);
            replace(temp, target);
        } catch (IOException e) {
            Files.deleteIfExists(temp);
            throw e;
        }
    }

    private void readDataLines(BufferedReader reader, List<String> headers, Path path,
            List<Map<String, String>> rows) throws IOException {
        int lineNumber = 1;
        String line = reader.readLine();
        while (line != null) {
            lineNumber++;
            if (!line.trim().isEmpty()) {
                List<String> fields = parseLine(line);
                if (fields == null || fields.size() != headers.size()) {
                    System.err.println("Warning: skipped malformed row at line " + lineNumber
                            + " of " + path.getFileName());
                } else {
                    rows.add(toRow(headers, fields));
                }
            }
            line = reader.readLine();
        }
    }

    private Map<String, String> toRow(List<String> headers, List<String> fields) {
        Map<String, String> row = new HashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            row.put(headers.get(i).trim(), fields.get(i));
        }
        return row;
    }

    private String stripByteOrderMark(String line) {
        if (!line.isEmpty() && line.charAt(0) == BYTE_ORDER_MARK) {
            return line.substring(1);
        }
        return line;
    }

    /**
     * Splits one line into fields, handling quotes.
     *
     * @return the fields, or null if a quote is never closed
     */
    private List<String> parseLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;
        boolean atFieldStart = true;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == QUOTE && i + 1 < line.length() && line.charAt(i + 1) == QUOTE) {
                    field.append(QUOTE);
                    i++;
                } else if (c == QUOTE) {
                    inQuotes = false;
                } else {
                    field.append(c);
                }
            } else if (c == QUOTE && atFieldStart) {
                inQuotes = true;
                atFieldStart = false;
            } else if (c == SEPARATOR) {
                fields.add(field.toString());
                field.setLength(0);
                atFieldStart = true;
            } else {
                field.append(c);
                atFieldStart = false;
            }
        }
        if (inQuotes) {
            return null;
        }
        fields.add(field.toString());
        return fields;
    }

    private void writeTemp(Path temp, String[] headers, List<Map<String, String>> rows)
            throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
            writer.write(joinHeaders(headers));
            writer.newLine();
            for (Map<String, String> row : rows) {
                writer.write(joinRow(headers, row));
                writer.newLine();
            }
        }
    }

    private String joinHeaders(String[] headers) {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < headers.length; i++) {
            if (i > 0) {
                line.append(SEPARATOR);
            }
            line.append(quoteIfNeeded(headers[i]));
        }
        return line.toString();
    }

    private String joinRow(String[] headers, Map<String, String> row) {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < headers.length; i++) {
            if (i > 0) {
                line.append(SEPARATOR);
            }
            String value = row.get(headers[i]);
            line.append(quoteIfNeeded(value == null ? "" : value));
        }
        return line.toString();
    }

    private String quoteIfNeeded(String value) {
        String text = value.replace('\r', ' ').replace('\n', ' ');
        if (text.indexOf(SEPARATOR) < 0 && text.indexOf(QUOTE) < 0) {
            return text;
        }
        return QUOTE + text.replace("\"", "\"\"") + QUOTE;
    }

    private void replace(Path temp, Path target) throws IOException {
        try {
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
