package medfinder.model;

/** A medicine in the catalogue. (Validation is added in task T05b.) */
public class Medicine {

    private final int id;
    private final String name;
    private final String category;
    private final String dosageForm;
    private final String strength;
    private final boolean prescriptionRequired;
    private final boolean active;

    /** Full constructor (used when loading from file). */
    public Medicine(int id, String name, String category, String dosageForm,
                    String strength, boolean prescriptionRequired, boolean active) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.dosageForm = dosageForm;
        this.strength = strength;
        this.prescriptionRequired = prescriptionRequired;
        this.active = active;
    }

    /** Short constructor: no prescription needed, and the medicine is active (chains with this(...)). */
    public Medicine(int id, String name, String category, String dosageForm, String strength) {
        this(id, name, category, dosageForm, strength, false, true);
    }

    /** @return the medicine id */
    public int getId() {
        return id;
    }

    /** @return the medicine name */
    public String getName() {
        return name;
    }

    /** @return the category, for example "Antibiotic" */
    public String getCategory() {
        return category;
    }

    /** @return the dosage form, for example "Tablet" */
    public String getDosageForm() {
        return dosageForm;
    }

    /** @return the strength, for example "500 mg" */
    public String getStrength() {
        return strength;
    }

    /** @return true if a prescription is required */
    public boolean isPrescriptionRequired() {
        return prescriptionRequired;
    }

    /** @return true if the medicine can appear in search and stock */
    public boolean isActive() {
        return active;
    }

    @Override
    public String toString() {
        return name + " " + strength + " (" + dosageForm + ")";
    }
}
