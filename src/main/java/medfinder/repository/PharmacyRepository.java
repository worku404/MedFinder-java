package medfinder.repository;

import java.util.List;

import medfinder.model.Pharmacy;
import medfinder.model.PharmacyStatus;

/**
 * Storage contract for pharmacies.
 *
 * <p>Every operation throws the unchecked {@code DataStorageException}
 * when the underlying storage cannot be read or written.
 */
public interface PharmacyRepository {

    /**
     * Finds a pharmacy by id.
     *
     * @param id the pharmacy id
     * @return the pharmacy, or null if there is none with that id
     */
    Pharmacy findById(int id);

    /**
     * Finds the pharmacy registered by a pharmacist.
     *
     * @param ownerUserId the id of the owning user
     * @return the pharmacy, or null if that user has none
     */
    Pharmacy findByOwner(int ownerUserId);

    /**
     * Finds a pharmacy by licence number, ignoring upper/lower case and spaces.
     *
     * @param licenceNumber the licence number to look for
     * @return the pharmacy, or null if no pharmacy has that licence
     */
    Pharmacy findByLicence(String licenceNumber);

    /**
     * Lists the pharmacies that have a given status.
     *
     * @param status the status to match
     * @return a copy of the matching pharmacies; empty if there are none
     */
    List<Pharmacy> findByStatus(PharmacyStatus status);

    /**
     * Inserts a new pharmacy, or updates the pharmacy that has the same id.
     *
     * @param pharmacy the pharmacy to store
     */
    void save(Pharmacy pharmacy);

    /**
     * Gives the next free pharmacy id.
     *
     * @return the largest existing id plus 1 (1 when there are no pharmacies)
     */
    int nextId();
}
