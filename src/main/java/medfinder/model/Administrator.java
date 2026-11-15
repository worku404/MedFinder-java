package medfinder.model;

/** The administrator who reviews pharmacies. */
public class Administrator extends User {

    /** Creates an administrator; see User for the rules. */
    public Administrator(int id, String fullName, String email, String passwordHash) {
        super(id, fullName, email, passwordHash);
    }

    @Override
    public Role role() {
        return Role.ADMINISTRATOR;
    }

    /** @return a short description without the password hash */
    @Override
    public String toString() {
        return "Administrator{id=" + getId() + ", name=" + getFullName() + ", email=" + getEmail() + "}";
    }
}
