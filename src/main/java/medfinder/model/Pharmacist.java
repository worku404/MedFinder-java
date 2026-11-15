package medfinder.model;

/** A registered pharmacist who owns at most one pharmacy. */
public class Pharmacist extends User {

    /** Creates a pharmacist; see User for the rules. */
    public Pharmacist(int id, String fullName, String email, String passwordHash) {
        super(id, fullName, email, passwordHash);
    }

    @Override
    public Role role() {
        return Role.PHARMACIST;
    }

    /** @return a short description without the password hash */
    @Override
    public String toString() {
        return "Pharmacist{id=" + getId() + ", name=" + getFullName() + ", email=" + getEmail() + "}";
    }
}
