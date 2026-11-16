package medfinder.model;

import java.time.Instant;

/**
 * The stock of one medicine in one pharmacy. Skeleton for task T05a: fields, constructor
 * and getters. reconfirm(now), update(...) and the validation are added in task T05b.
 */
public class StockRecord {

    private final int pharmacyId;
    private final int medicineId;
    private int quantity;
    private double price;
    private Instant lastConfirmedAt;

    /** Creates a record. For a new record the caller passes the current time as lastConfirmedAt. */
    public StockRecord(int pharmacyId, int medicineId, int quantity, double price,
                       Instant lastConfirmedAt) {
        this.pharmacyId = pharmacyId;
        this.medicineId = medicineId;
        this.quantity = quantity;
        this.price = price;
        this.lastConfirmedAt = lastConfirmedAt;
    }

    /** @return the pharmacy id */
    public int getPharmacyId() {
        return pharmacyId;
    }

    /** @return the medicine id */
    public int getMedicineId() {
        return medicineId;
    }

    /** @return the quantity in stock */
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
