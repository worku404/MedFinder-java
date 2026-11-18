package medfinder.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import medfinder.exception.ValidationException;

/** Local helper tests (not presented). Tests for the Location value object. */
public class LocationTest {

    @Test
    public void sameRegionAndDistrictIsSameDistrict() {
        Location a = new Location("Test Region", "North");
        Location b = new Location("Test Region", "North");
        assertEquals(MatchLevel.SAME_DISTRICT, a.matchLevel(b));
    }

    @Test
    public void differentDistrictIsOtherArea() {
        Location a = new Location("Test Region", "North");
        Location b = new Location("Test Region", "South");
        assertEquals(MatchLevel.OTHER_AREA, a.matchLevel(b));
    }

    @Test
    public void differentRegionWithSameDistrictNameIsOtherArea() {
        Location a = new Location("Region One", "North");
        Location b = new Location("Region Two", "North");
        assertEquals(MatchLevel.OTHER_AREA, a.matchLevel(b));
    }

    @Test
    public void nullOtherLocationIsOtherArea() {
        assertEquals(MatchLevel.OTHER_AREA, new Location("R", "D").matchLevel(null));
    }

    @Test
    public void caseAndExtraSpacesAreIgnored() {
        Location a = new Location("Test Region", "North East");
        Location b = new Location("  test   REGION ", " north    east  ");
        assertTrue(a.equals(b));
        assertEquals(a.hashCode(), b.hashCode());
        assertEquals(MatchLevel.SAME_DISTRICT, a.matchLevel(b));
    }

    @Test
    public void equalsAndHashCodeFollowTheRules() {
        Location a = new Location("R", "D");
        assertTrue(a.equals(a));
        assertFalse(a.equals(null));
        assertFalse(a.equals("R, D"));
        assertFalse(a.equals(new Location("R", "E")));
    }

    @Test
    public void getValuesAreTrimmed() {
        Location a = new Location("  R ", " D  ");
        assertEquals("R", a.getRegion());
        assertEquals("D", a.getDistrict());
        assertEquals("D, R", a.toString());
    }

    @Test
    public void emptyRegionIsRefused() {
        try {
            new Location("  ", "D");
            fail("Expected ValidationException");
        } catch (ValidationException e) {
            assertTrue(e.getMessage().contains("Region"));
        }
    }

    @Test
    public void emptyOrNullDistrictIsRefused() {
        try {
            new Location("R", null);
            fail("Expected ValidationException");
        } catch (ValidationException e) {
            assertTrue(e.getMessage().contains("Sub-city"));
        }
        try {
            new Location("R", "");
            fail("Expected ValidationException");
        } catch (ValidationException e) {
            assertTrue(e.getMessage().contains("Sub-city"));
        }
    }
}
