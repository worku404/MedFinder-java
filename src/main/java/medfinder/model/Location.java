package medfinder.model;

import java.util.Objects;

import medfinder.exception.ValidationException;

/**
 * An immutable value object: a region and a district (sub-city).
 * Two locations are equal when region and district match, ignoring case and extra spaces.
 */
public final class Location {

    private final String region;
    private final String district;

    /**
     * @param region   the region, for example the active region from the settings
     * @param district the district (sub-city)
     * @throws ValidationException if region or district is null or empty
     */
    public Location(String region, String district) {
        if (region == null || region.trim().isEmpty()) {
            throw new ValidationException("Region must not be empty.");
        }
        if (district == null || district.trim().isEmpty()) {
            throw new ValidationException("Sub-city must not be empty.");
        }
        this.region = region.trim();
        this.district = district.trim();
    }

    /** @return the region */
    public String getRegion() {
        return region;
    }

    /** @return the district (shown to users as "Sub-city") */
    public String getDistrict() {
        return district;
    }

    /**
     * Compares this location with another one.
     *
     * @param other the other location (for example the customer's)
     * @return SAME_DISTRICT when region and district are equal, otherwise OTHER_AREA
     *         (also OTHER_AREA when other is null)
     */
    public MatchLevel matchLevel(Location other) {
        if (equals(other)) {
            return MatchLevel.SAME_DISTRICT;
        }
        return MatchLevel.OTHER_AREA;
    }

    private static String normalize(String text) {
        return text.trim().toLowerCase().replaceAll("\\s+", " ");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Location)) {
            return false;
        }
        Location other = (Location) obj;
        return normalize(region).equals(normalize(other.region))
                && normalize(district).equals(normalize(other.district));
    }

    @Override
    public int hashCode() {
        return Objects.hash(normalize(region), normalize(district));
    }

    @Override
    public String toString() {
        return district + ", " + region;
    }
}
