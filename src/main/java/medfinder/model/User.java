package medfinder.model;

import medfinder.exception.ValidationException;

/**
 * A registered account. Abstract: only Pharmacist and Administrator exist.
 * Customers are anonymous and have no User object.
 */
public abstract class User {

    private final int id;
    private final String fullName;
    private final String email;
    private final String passwordHash;

    /**
     * @param id           positive id
     * @param fullName     not empty
     * @param email        not empty (the format is checked by AuthService)
     * @param passwordHash not empty, stored as salt:hash
     * @throws ValidationException if a value is missing
     */
    protected User(int id, String fullName, String email, String passwordHash) {
        if (id <= 0) {
            throw new ValidationException("User id must be a positive number.");
        }
        this.id = id;
        this.fullName = requireText(fullName, "Full name");
        this.email = requireText(email, "Email");
        this.passwordHash = requireText(passwordHash, "Password");
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " must not be empty.");
        }
        return value.trim();
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
