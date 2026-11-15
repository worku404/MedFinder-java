package medfinder.model;

/**
 * A registered account. Abstract: only Pharmacist and Administrator exist.
 * Customers are anonymous and have no User object.
 * (Validation of the fields is added in task T05b.)
 */
public abstract class User {

    private final int id;
    private final String fullName;
    private final String email;
    private final String passwordHash;

    /** Creates a user with the given values. */
    protected User(int id, String fullName, String email, String passwordHash) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    /** @return the account id */
    public int getId() {
        return id;
    }

    /** @return the full name */
    public String getFullName() {
        return fullName;
    }

    /** @return the email address */
    public String getEmail() {
        return email;
    }

    /** @return the stored password hash (never print it) */
    public String getPasswordHash() {
        return passwordHash;
    }

    /** @return the role of this account; each subclass decides */
    public abstract Role role();
}
