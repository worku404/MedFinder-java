package medfinder.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import medfinder.exception.ValidationException;

/** Local helper tests (not presented). Every transition of SRS BR-11 and the other entity rules. */
public class PharmacyTest {

    private static final Location NORTH = new Location("Test Region", "North");
    private static final Location SOUTH = new Location("Test Region", "South");

    private Pharmacy pending() {
        return new Pharmacy(1, "Alpha Pharmacy", "LIC-001", NORTH, "Near the market", 7);
    }

    private Pharmacy verified() {
        Pharmacy p = pending();
        p.approve();
        return p;
    }

    private Pharmacy rejected() {
        Pharmacy p = pending();
        p.reject("Licence unreadable");
        return p;
    }

    private void expectRefused(Pharmacy p, String action) {
        try {
            if (action.equals("approve")) {
                p.approve();
            } else if (action.equals("reject")) {
                p.reject("Some reason");
            } else {
                p.resubmit();
            }
            fail("Expected ValidationException for " + action);
        } catch (ValidationException e) {
            assertTrue(e.getMessage().length() > 0);
        }
    }

    // ---- new pharmacy ----

    @Test
    public void newPharmacyIsPendingWithoutReason() {
        Pharmacy p = pending();
        assertEquals(PharmacyStatus.PENDING, p.getStatus());
        assertNull(p.getRejectionReason());
        assertEquals(7, p.getOwnerUserId());
    }

    // ---- allowed transitions ----

    @Test
    public void pendingCanBeApproved() {
        Pharmacy p = pending();
        p.approve();
        assertEquals(PharmacyStatus.VERIFIED, p.getStatus());
    }

    @Test
    public void pendingCanBeRejectedWithReason() {
        Pharmacy p = pending();
        p.reject("  Licence unreadable ");
        assertEquals(PharmacyStatus.REJECTED, p.getStatus());
        assertEquals("Licence unreadable", p.getRejectionReason());
    }

    @Test
    public void verifiedCanBeWithdrawnWithReason() {
        Pharmacy p = verified();
        p.reject("Closed down");
        assertEquals(PharmacyStatus.REJECTED, p.getStatus());
        assertEquals("Closed down", p.getRejectionReason());
    }

    @Test
    public void rejectedCanBeResubmittedAndReasonIsCleared() {
        Pharmacy p = rejected();
        p.resubmit();
        assertEquals(PharmacyStatus.PENDING, p.getStatus());
        assertNull(p.getRejectionReason());
    }

    // ---- refused transitions ----

    @Test
    public void verifiedAndRejectedCannotBeApproved() {
        expectRefused(verified(), "approve");
        expectRefused(rejected(), "approve");
    }

    @Test
    public void rejectedCannotBeRejectedAgain() {
        expectRefused(rejected(), "reject");
    }

    @Test
    public void pendingAndVerifiedCannotBeResubmitted() {
        expectRefused(pending(), "resubmit");
        expectRefused(verified(), "resubmit");
    }

    @Test
    public void rejectWithoutReasonIsRefusedAndStatusStays() {
        Pharmacy p = pending();
        try {
            p.reject("   ");
            fail("Expected ValidationException");
        } catch (ValidationException e) {
            assertTrue(e.getMessage().contains("reason"));
        }
        try {
            p.reject(null);
            fail("Expected ValidationException");
        } catch (ValidationException e) {
            assertTrue(e.getMessage().contains("reason"));
        }
        assertEquals(PharmacyStatus.PENDING, p.getStatus());
        assertNull(p.getRejectionReason());
    }

    @Test
    public void withdrawWithoutReasonIsRefusedAndStaysVerified() {
        Pharmacy p = verified();
        try {
            p.reject("");
            fail("Expected ValidationException");
        } catch (ValidationException e) {
            assertTrue(e.getMessage().contains("reason"));
        }
        assertEquals(PharmacyStatus.VERIFIED, p.getStatus());
    }

    // ---- changeDetails (AT-22) ----

    @Test
    public void nameOrAddressChangeKeepsVerified() {
        Pharmacy p = verified();
        p.changeDetails("Alpha Plus", "LIC-001", NORTH, "New landmark");
        assertEquals(PharmacyStatus.VERIFIED, p.getStatus());
        assertEquals("Alpha Plus", p.getName());
        assertEquals("New landmark", p.getAddress());
    }

    @Test
    public void licenceChangeReturnsVerifiedToPending() {
        Pharmacy p = verified();
        p.changeDetails("Alpha Pharmacy", "LIC-002", NORTH, "Near the market");
        assertEquals(PharmacyStatus.PENDING, p.getStatus());
        assertEquals("LIC-002", p.getLicenceNumber());
    }

    @Test
    public void licenceChangeOnlyInCaseOrSpacesKeepsVerified() {
        Pharmacy p = verified();
        p.changeDetails("Alpha Pharmacy", " lic-001 ", NORTH, "Near the market");
        assertEquals(PharmacyStatus.VERIFIED, p.getStatus());
    }

    @Test
    public void locationChangeReturnsVerifiedToPending() {
        Pharmacy p = verified();
        p.changeDetails("Alpha Pharmacy", "LIC-001", SOUTH, "Near the market");
        assertEquals(PharmacyStatus.PENDING, p.getStatus());
        assertEquals(SOUTH, p.getLocation());
    }

    @Test
    public void editingPendingOrRejectedKeepsTheStatus() {
        Pharmacy a = pending();
        a.changeDetails("Alpha", "LIC-009", SOUTH, "X");
        assertEquals(PharmacyStatus.PENDING, a.getStatus());

        Pharmacy b = rejected();
        b.changeDetails("Alpha", "LIC-009", SOUTH, "X");
        assertEquals(PharmacyStatus.REJECTED, b.getStatus());
        assertEquals("Licence unreadable", b.getRejectionReason());
    }

    @Test
    public void badValueInChangeDetailsChangesNothing() {
        Pharmacy p = verified();
        try {
            p.changeDetails("Beta", "LIC-777", SOUTH, "  ");
            fail("Expected ValidationException");
        } catch (ValidationException e) {
            assertTrue(e.getMessage().contains("Address"));
        }
        assertEquals("Alpha Pharmacy", p.getName());
        assertEquals("LIC-001", p.getLicenceNumber());
        assertEquals(NORTH, p.getLocation());
        assertEquals(PharmacyStatus.VERIFIED, p.getStatus());
    }

    // ---- constructor rules (BR-06) ----

    @Test
    public void emptyFieldsAreRefusedInConstructor() {
        try {
            new Pharmacy(1, " ", "LIC", NORTH, "A", 7);
            fail("Expected ValidationException");
        } catch (ValidationException e) {
            assertTrue(e.getMessage().contains("name"));
        }
        try {
            new Pharmacy(1, "N", "", NORTH, "A", 7);
            fail("Expected ValidationException");
        } catch (ValidationException e) {
            assertTrue(e.getMessage().contains("Licence"));
        }
        try {
            new Pharmacy(1, "N", "LIC", null, "A", 7);
            fail("Expected ValidationException");
        } catch (ValidationException e) {
            assertTrue(e.getMessage().contains("Sub-city"));
        }
    }

    @Test
    public void loadingRejectedWithoutReasonIsRefused() {
        try {
            new Pharmacy(2, "N", "LIC", NORTH, "A", PharmacyStatus.REJECTED, "", 7);
            fail("Expected ValidationException");
        } catch (ValidationException e) {
            assertTrue(e.getMessage().contains("reason"));
        }
    }

    @Test
    public void loadingVerifiedWithReasonIsRefused() {
        try {
            new Pharmacy(2, "N", "LIC", NORTH, "A", PharmacyStatus.VERIFIED, "oops", 7);
            fail("Expected ValidationException");
        } catch (ValidationException e) {
            assertTrue(e.getMessage().contains("reason"));
        }
    }

    @Test
    public void loadingBlankReasonForVerifiedMeansNone() {
        Pharmacy p = new Pharmacy(2, "N", "LIC", NORTH, "A", PharmacyStatus.VERIFIED, "  ", 7);
        assertNull(p.getRejectionReason());
        Pharmacy q = new Pharmacy(3, "N", "LIC", NORTH, "A", PharmacyStatus.REJECTED, "Bad", 7);
        assertEquals("Bad", q.getRejectionReason());
    }
}
