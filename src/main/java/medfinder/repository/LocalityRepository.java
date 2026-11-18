package medfinder.repository;

import java.util.List;

/**
 * Storage contract for the regions and their districts (sub-cities).
 * Districts are plain text, so no entity class is needed.
 *
 * <p>Every operation throws the unchecked {@code DataStorageException}
 * when the underlying storage cannot be read.
 */
public interface LocalityRepository {

    /**
     * Lists the districts of a region.
     *
     * @param region the region name
     * @return a copy of the district names; empty if the region is unknown
     */
    List<String> findDistricts(String region);

    /**
     * Tells whether a district exists in a region.
     *
     * @param region the region name
     * @param district the district name
     * @return true if the region has that district
     */
    boolean exists(String region, String district);
}
