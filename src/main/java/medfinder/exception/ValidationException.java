package medfinder.exception;

/**
 * A field is empty, malformed, out of range, or a duplicate (email, licence, second pharmacy, stock record).
 */
public class ValidationException extends MedFinderException {

    /** Creates the exception with a message for the end user. */
    public ValidationException(String message) {
        super(message);
    }

    /** Creates the exception with a message and the original cause. */
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
