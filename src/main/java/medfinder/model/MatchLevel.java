package medfinder.model;

/** Whether a pharmacy is in the customer's own sub-city. The code word is "district". */
public enum MatchLevel {
    SAME_DISTRICT("Same sub-city"),
    OTHER_AREA("Other area");

    private final String label;

    MatchLevel(String label) {
        this.label = label;
    }

    /** @return the text shown to users, for example "Same sub-city" */
    public String label() {
        return label;
    }
}
