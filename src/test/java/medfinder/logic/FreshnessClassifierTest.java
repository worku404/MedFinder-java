package medfinder.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;

import medfinder.exception.ValidationException;
import medfinder.model.Freshness;

/** Local helper tests (not presented). Boundary tests for FreshnessClassifier. */
public class FreshnessClassifierTest {

    private static final Instant NOW = Instant.parse("2026-11-15T12:00:00Z");

    private Freshness ageOf(Duration age) {
        return FreshnessClassifier.classify(NOW.minus(age), NOW);
    }

    @Test
    public void elevenHoursFiftyNineMinutesIsFresh() {
        assertEquals(Freshness.FRESH, ageOf(Duration.ofHours(11).plusMinutes(59)));
    }

    @Test
    public void exactlyTwelveHoursIsFresh() {
        assertEquals(Freshness.FRESH, ageOf(Duration.ofHours(12)));
    }

    @Test
    public void justOverTwelveHoursIsAging() {
        assertEquals(Freshness.AGING, ageOf(Duration.ofHours(12).plusSeconds(1)));
    }

    @Test
    public void fortySevenHoursIsAging() {
        assertEquals(Freshness.AGING, ageOf(Duration.ofHours(47)));
    }

    @Test
    public void exactlyFortyEightHoursIsAging() {
        assertEquals(Freshness.AGING, ageOf(Duration.ofHours(48)));
    }

    @Test
    public void fortyNineHoursIsStale() {
        assertEquals(Freshness.STALE, ageOf(Duration.ofHours(49)));
    }

    @Test
    public void sampleDataAges5And30And60Hours() {
        assertEquals(Freshness.FRESH, ageOf(Duration.ofHours(5)));
        assertEquals(Freshness.AGING, ageOf(Duration.ofHours(30)));
        assertEquals(Freshness.STALE, ageOf(Duration.ofHours(60)));
    }

    @Test
    public void justConfirmedIsFresh() {
        assertEquals(Freshness.FRESH, FreshnessClassifier.classify(NOW, NOW));
    }

    @Test
    public void nullTimeIsRefused() {
        try {
            FreshnessClassifier.classify(null, NOW);
            fail("Expected ValidationException");
        } catch (ValidationException e) {
            assertEquals("Both times are needed to work out freshness.", e.getMessage());
        }
    }
}
