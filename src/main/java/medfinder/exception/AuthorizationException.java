package medfinder.exception;

/**
 * The user is not logged in, has the wrong role, gave wrong credentials, or the pharmacy is not Verified.
 */
public class AuthorizationException extends MedFinderException {

    /** Creates the exception with a message for the end user. */
    public AuthorizationException(String message) {
        super(message);
    }

    /** Creates the exception with a message and the original cause. */
    public AuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }
}
