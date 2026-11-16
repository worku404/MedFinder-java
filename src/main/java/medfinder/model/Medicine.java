package medfinder.model;

import medfinder.exception.ValidationException;

/** A medicine in the catalogue. */
public class Medicine {

    private final int id;
    private final String name;
    private final String category;
    private final String dosageForm;
    private final String strength;
    private final boolean prescriptionRequired;
    private final boolean active;

    /**
     * Full constructor (used when loading from file).
     *
     * @throws ValidationException if id is not positive, or name, category or dosage form is empty
     */
    public Medicine(int id, String name, String category, String dosageForm,
                    String strength, boolean prescriptionRequired, boolean active) {
        if (id <= 0) {
            throw new ValidationException("Medicine id must be a positive number.");
        }
        this.id = id;
        this.name = requireText(name, "Medicine name");
        this.category = requireText(category, "Category");
        this.dosageForm = requireText(dosageForm, "Dosage form");
        this.strength = (strength == null) ? "" : strength.trim();
        this.prescriptionRequired = prescriptionRequired;
        this.active = active;
    }

    /** Short constructor: no prescription needed, and the medicine is active (chains with this(...)). */
    public Medicine(int id, String name, String category, String dosageForm, String strength) {
        this(id, name, category, dosageForm, strength, false, true);
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " must not be empty.");
        }
        return value.trim();
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

    /** @return the strength, for example "500 mg" (empty text if none) */
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
