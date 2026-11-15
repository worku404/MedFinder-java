package medfinder.exception;

/**
 * Parent of all MedFinder exceptions. It is unchecked (extends RuntimeException),
 * so service methods do not need long "throws" lists.
 */
public class MedFinderException extends RuntimeException {

    /** Creates an exception with no message. */
    public MedFinderException() {
        super();
    }

    /** Creates an exception with a message for the end user. */
    public MedFinderException(String message) {
        super(message);
    }

    /** Creates an exception with a message and the original cause (exception chaining). */
    public MedFinderException(String message, Throwable cause) {
        super(message, cause);
    }

    /** Creates an exception that only wraps a cause. */
    public MedFinderException(Throwable cause) {
        super(cause);
    }
}
