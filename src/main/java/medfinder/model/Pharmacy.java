package medfinder.model;

import medfinder.exception.ValidationException;

/**
 * A pharmacy. It owns its status rules (SRS BR-11): there is no public status setter,
 * only approve(), reject(), resubmit() and changeDetails().
 * The service decides WHO may act; this class decides WHAT is allowed.
 */
public class Pharmacy {

    private final int id;
    private String name;
    private String licenceNumber;
    private Location location;
    private String address;
    private PharmacyStatus status;
    private String rejectionReason;
    private final int ownerUserId;

    /**
     * New registration: status is PENDING and there is no rejection reason.
     *
     * @throws ValidationException if a value is missing or the id is not positive
     */
    public Pharmacy(int id, String name, String licenceNumber, Location location,
                    String address, int ownerUserId) {
        this(id, name, licenceNumber, location, address, PharmacyStatus.PENDING, null, ownerUserId);
    }

    /**
     * Full constructor, used when loading from file. A REJECTED pharmacy must have a reason;
     * a PENDING or VERIFIED one must have none (BR-06). A null or blank reason means "none".
     *
     * @throws ValidationException if a value is missing or BR-06 is broken
     */
    public Pharmacy(int id, String name, String licenceNumber, Location location,
                    String address, PharmacyStatus status, String rejectionReason,
                    int ownerUserId) {
        if (id <= 0) {
            throw new ValidationException("Pharmacy id must be a positive number.");
        }
        if (ownerUserId <= 0) {
            throw new ValidationException("Pharmacy owner must be a valid user.");
        }
        if (status == null) {
            throw new ValidationException("Pharmacy status must not be empty.");
        }
        this.id = id;
        this.ownerUserId = ownerUserId;
        this.name = requireText(name, "Pharmacy name");
        this.licenceNumber = requireText(licenceNumber, "Licence number");
        this.location = requireLocation(location);
        this.address = requireText(address, "Address");
        this.status = status;
        this.rejectionReason = checkReason(status, rejectionReason);
    }

    // ---- status rules -------------------------------------------------------------

    /**
     * PENDING becomes VERIFIED.
     *
     * @throws ValidationException if the pharmacy is not PENDING
     */
    public void approve() {
        if (status != PharmacyStatus.PENDING) {
            throw new ValidationException("Only a pending pharmacy can be approved.");
        }
        status = PharmacyStatus.VERIFIED;
    }

    /**
     * PENDING or VERIFIED becomes REJECTED, with a reason.
     *
     * @param reason why the pharmacy is rejected or withdrawn; must not be empty
     * @throws ValidationException if the status does not allow it or the reason is empty
     */
    public void reject(String reason) {
        if (status != PharmacyStatus.PENDING && status != PharmacyStatus.VERIFIED) {
            throw new ValidationException("Only a pending or verified pharmacy can be rejected.");
        }
        String cleanReason = requireText(reason, "Rejection reason");
        status = PharmacyStatus.REJECTED;
        rejectionReason = cleanReason;
    }

    /**
     * REJECTED becomes PENDING again and the reason is cleared.
     *
     * @throws ValidationException if the pharmacy is not REJECTED
     */
    public void resubmit() {
        if (status != PharmacyStatus.REJECTED) {
            throw new ValidationException("Only a rejected pharmacy can be resubmitted.");
        }
        status = PharmacyStatus.PENDING;
        rejectionReason = null;
    }

    /**
     * Changes the details. All values are checked first, so a bad value changes nothing.
     * A VERIFIED pharmacy goes back to PENDING when the licence number or the location changes
     * (BR-11); a change of name or address only keeps it VERIFIED.
     *
     * @throws ValidationException if a value is empty
     */
    public void changeDetails(String name, String licenceNumber, Location location, String address) {
        String newName = requireText(name, "Pharmacy name");
        String newLicence = requireText(licenceNumber, "Licence number");
        Location newLocation = requireLocation(location);
        String newAddress = requireText(address, "Address");

        boolean licenceChanged = !normalizeLicence(newLicence).equals(normalizeLicence(this.licenceNumber));
        boolean locationChanged = !newLocation.equals(this.location);

        this.name = newName;
        this.licenceNumber = newLicence;
        this.location = newLocation;
        this.address = newAddress;

        if (status == PharmacyStatus.VERIFIED && (licenceChanged || locationChanged)) {
            status = PharmacyStatus.PENDING;
        }
    }

    // ---- getters ------------------------------------------------------------------

    /** @return the pharmacy id */
    public int getId() {
        return id;
    }

    /** @return the pharmacy name */
    public String getName() {
        return name;
    }

    /** @return the licence number (shown only to the owner and the administrator) */
    public String getLicenceNumber() {
        return licenceNumber;
    }

    /** @return the location (immutable, so it is safe to return) */
    public Location getLocation() {
        return location;
    }

    /** @return the address text, for example a landmark */
    public String getAddress() {
        return address;
    }

    /** @return the current status */
    public PharmacyStatus getStatus() {
        return status;
    }

    /** @return the rejection reason, or null if the pharmacy is not REJECTED */
    public String getRejectionReason() {
        return rejectionReason;
    }

    /** @return the id of the pharmacist who owns this pharmacy */
    public int getOwnerUserId() {
        return ownerUserId;
    }

    // ---- private helpers ----------------------------------------------------------

    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " must not be empty.");
        }
        return value.trim();
    }

    private static Location requireLocation(Location value) {
        if (value == null) {
            throw new ValidationException("Sub-city must be chosen.");
        }
        return value;
    }

    private static String checkReason(PharmacyStatus status, String reason) {
        boolean hasReason = reason != null && !reason.trim().isEmpty();
        if (status == PharmacyStatus.REJECTED) {
            if (!hasReason) {
                throw new ValidationException("A rejected pharmacy must have a reason.");
            }
            return reason.trim();
        }
        if (hasReason) {
            throw new ValidationException("Only a rejected pharmacy can have a rejection reason.");
        }
        return null;
    }

    private static String normalizeLicence(String text) {
        return text.replaceAll("\\s+", "").toLowerCase();
    }

    @Override
    public String toString() {
        return name + " (" + location + ", " + status.label() + ")";
    }
}
