package medfinder.model;

import java.time.Instant;

import medfinder.exception.ValidationException;

/** The stock of one medicine in one pharmacy: quantity, price and when it was last confirmed. */
public class StockRecord {

    private final int pharmacyId;
    private final int medicineId;
    private int quantity;
    private double price;
    private Instant lastConfirmedAt;

    /**
     * Creates a record. For a new record the caller passes the current time as lastConfirmedAt.
     *
     * @param quantity        0 or more
     * @param price           more than 0, in Birr
     * @param lastConfirmedAt when the stock was last confirmed (UTC)
     * @throws ValidationException if an id is not positive, quantity is negative,
     *                             price is not above 0, or the time is missing
     */
    public StockRecord(int pharmacyId, int medicineId, int quantity, double price,
                       Instant lastConfirmedAt) {
        if (pharmacyId <= 0 || medicineId <= 0) {
            throw new ValidationException("Pharmacy and medicine must be valid.");
        }
        checkQuantityAndPrice(quantity, price);
        this.pharmacyId = pharmacyId;
        this.medicineId = medicineId;
        this.quantity = quantity;
        this.price = price;
        this.lastConfirmedAt = requireTime(lastConfirmedAt);
    }

    /**
     * Sets last-confirmed to now and keeps quantity and price unchanged (AT-06).
     *
     * @throws ValidationException if now is null
     */
    public void reconfirm(Instant now) {
        lastConfirmedAt = requireTime(now);
    }

    /**
     * Changes quantity and price and sets last-confirmed to now. All values are checked first.
     *
     * @throws ValidationException if a value is not allowed
     */
    public void update(int quantity, double price, Instant now) {
        checkQuantityAndPrice(quantity, price);
        Instant checkedNow = requireTime(now);
        this.quantity = quantity;
        this.price = price;
        this.lastConfirmedAt = checkedNow;
    }

    private static void checkQuantityAndPrice(int quantity, double price) {
        if (quantity < 0) {
            throw new ValidationException("Quantity must be 0 or more.");
        }
        if (!(price > 0)) {
            throw new ValidationException("Price must be more than 0.");
        }
    }

    private static Instant requireTime(Instant time) {
        if (time == null) {
            throw new ValidationException("The confirmation time must not be empty.");
        }
        return time;
    }

    /** @return the pharmacy id */
    public int getPharmacyId() {
        return pharmacyId;
    }

    /** @return the medicine id */
    public int getMedicineId() {
        return medicineId;
    }

    /** @return the quantity in stock (0 or more) */
    public int getQuantity() {
        return quantity;
    }

    /** @return the price in Birr */
    public double getPrice() {
        return price;
    }

    /** @return when the stock was last confirmed (UTC) */
    public Instant getLastConfirmedAt() {
        return lastConfirmedAt;
    }

    @Override
    public String toString() {
        return "StockRecord{pharmacy=" + pharmacyId + ", medicine=" + medicineId
                + ", quantity=" + quantity + ", price=" + price + "}";
    }
}
