package medfinder.repository;

import java.util.List;

import medfinder.model.Medicine;

/**
 * Storage contract for the medicine catalogue (read only).
 *
 * <p>Every operation throws the unchecked {@code DataStorageException}
 * when the underlying storage cannot be read.
 */
public interface MedicineRepository {

    /**
     * Finds a medicine by id.
     *
     * @param id the medicine id
     * @return the medicine, or null if there is none with that id
     */
    Medicine findById(int id);

    /**
     * Lists the medicines that are active in the catalogue.
     *
     * @return a copy of the active medicines; empty if there are none
     */
    List<Medicine> findActive();
}
