package medfinder.repository;

import java.util.List;

import medfinder.model.StockRecord;

/**
 * Storage contract for stock records (one record per pharmacy and medicine).
 *
 * <p>Every operation throws the unchecked {@code DataStorageException}
 * when the underlying storage cannot be read or written.
 */
public interface StockRepository {

    /**
     * Lists the stock records of one pharmacy.
     *
     * @param pharmacyId the pharmacy id
     * @return a copy of the pharmacy's records; empty if there are none
     */
    List<StockRecord> findByPharmacy(int pharmacyId);

    /**
     * Lists the stock records of one medicine across all pharmacies.
     *
     * @param medicineId the medicine id
     * @return a copy of the medicine's records; empty if there are none
     */
    List<StockRecord> findByMedicine(int medicineId);

    /**
     * Finds the record for one pharmacy and one medicine.
     *
     * @param pharmacyId the pharmacy id
     * @param medicineId the medicine id
     * @return the record, or null if the pharmacy does not stock that medicine
     */
    StockRecord find(int pharmacyId, int medicineId);

    /**
     * Inserts a record, or replaces the record with the same pharmacy and medicine.
     *
     * @param record the record to store
     */
    void save(StockRecord record);

    /**
     * Deletes the record for one pharmacy and one medicine. Does nothing if there is none.
     *
     * @param pharmacyId the pharmacy id
     * @param medicineId the medicine id
     */
    void delete(int pharmacyId, int medicineId);

    /**
     * Inserts or replaces many records and writes the storage once.
     *
     * @param records the records to store
     */
    void saveAll(List<StockRecord> records);
}
