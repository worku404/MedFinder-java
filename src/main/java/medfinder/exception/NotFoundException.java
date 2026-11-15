package medfinder.exception;

/**
 * An id was given that does not exist.
 */
public class NotFoundException extends MedFinderException {

    /** Creates the exception with a message for the end user. */
    public NotFoundException(String message) {
        super(message);
    }

    /** Creates the exception with a message and the original cause. */
    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
