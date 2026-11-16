package medfinder.model;

/** How recently a stock record was confirmed. Each value has a label and a ranking value. */
public enum Freshness {
    FRESH("Fresh", 1.0),
    AGING("Aging", 0.6),
    STALE("Stale", 0.2);

    private final String label;
    private final double value;

    Freshness(String label, double value) {
        this.label = label;
        this.value = value;
    }

    /** @return the text shown to users, for example "Fresh" */
    public String label() {
        return label;
    }

    /** @return the number used in the search score (Fresh 1.0, Aging 0.6, Stale 0.2) */
    public double value() {
        return value;
    }
}
