package medfinder.model;

/** The two kinds of registered account. Customers have no account. */
public enum Role {
    PHARMACIST("Pharmacist"),
    ADMINISTRATOR("Administrator");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    /** @return the text shown to users, for example "Pharmacist" */
    public String label() {
        return label;
    }
}
