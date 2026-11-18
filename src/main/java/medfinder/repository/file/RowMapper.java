package medfinder.repository.file;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Converts between a CSV row and an entity. A row is a map from column name
 * to text, as returned by {@link CsvFile}.
 *
 * <p>This is the only generic type declared in the project. Each file
 * repository implements it next to its repository interface, for example
 * {@code FileUserRepository implements UserRepository, RowMapper<User>}.
 *
 * @param <T> the entity type that a row describes
 */
public interface RowMapper<T> {

    /**
     * Gives the column names in the order they are written to the file.
     *
     * @return the header names
     */
    String[] headers();

    /**
     * Builds an entity from one row.
     *
     * @param row column name to text
     * @return the entity
     * @throws RuntimeException if the row is malformed (a missing column, a bad number, a rule
     *                          broken); {@link #mapAll} turns this into a warning
     */
    T fromRow(Map<String, String> row);

    /**
     * Turns an entity into one row, with a value for every header.
     *
     * @param entity the entity to write
     * @return column name to text
     */
    Map<String, String> toRow(T entity);

    /**
     * Converts every row into an entity. A malformed row is skipped and a
     * one-line warning is printed to the error stream ({@code System.err}),
     * so the program still starts (FR-DAT-03). There is no log file.
     *
     * @param rows the rows read by {@link CsvFile}
     * @return the entities of the good rows; empty if there are none, never null
     */
    default List<T> mapAll(List<Map<String, String>> rows) {
        List<T> result = new ArrayList<>();
        if (rows == null) {
            return result;
        }
        int position = 1;
        for (Map<String, String> row : rows) {
            try {
                result.add(fromRow(row));
            } catch (RuntimeException e) {
                System.err.println("Warning: skipped malformed data row " + position
                        + " (" + e.getMessage() + ")");
            }
            position++;
        }
        return result;
    }
}
