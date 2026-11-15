package medfinder.model;

/** The review state of a pharmacy (SRS BR-11). */
public enum PharmacyStatus {
    PENDING("Pending"),
    VERIFIED("Verified"),
    REJECTED("Rejected");

    private final String label;

    PharmacyStatus(String label) {
        this.label = label;
    }

    /** @return the text shown to users, for example "Pending" */
    public String label() {
        return label;
    }
}
