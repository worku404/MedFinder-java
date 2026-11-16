package medfinder.model;

/**
 * A pharmacy. Skeleton for task T05a: fields, constructors and getters only.
 * The status methods approve(), reject(), resubmit(), changeDetails() and the
 * validation are added in task T05b. There will be no public status setter.
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

    /** New registration: status is PENDING and there is no rejection reason. */
    public Pharmacy(int id, String name, String licenceNumber, Location location,
                    String address, int ownerUserId) {
        this(id, name, licenceNumber, location, address, PharmacyStatus.PENDING, null, ownerUserId);
    }

    /** Full constructor, used when loading from file. */
    public Pharmacy(int id, String name, String licenceNumber, Location location,
                    String address, PharmacyStatus status, String rejectionReason,
                    int ownerUserId) {
        this.id = id;
        this.name = name;
        this.licenceNumber = licenceNumber;
        this.location = location;
        this.address = address;
        this.status = status;
        this.rejectionReason = rejectionReason;
        this.ownerUserId = ownerUserId;
    }

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

    @Override
    public String toString() {
        return name + " (" + location + ", " + status.label() + ")";
    }
}
