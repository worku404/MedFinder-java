package medfinder.exception;

/**
 * A data file cannot be read or written (wraps the checked IOException).
 */
public class DataStorageException extends MedFinderException {

    /** Creates the exception with a message for the end user. */
    public DataStorageException(String message) {
        super(message);
    }

    /** Creates the exception with a message and the original cause. */
    public DataStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
