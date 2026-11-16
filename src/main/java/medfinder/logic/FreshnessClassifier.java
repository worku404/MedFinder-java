package medfinder.logic;

import java.time.Duration;
import java.time.Instant;

import medfinder.exception.ValidationException;
import medfinder.model.Freshness;

/**
 * Decides how fresh a stock record is. A pure static function: the current time is
 * passed in, so tests can fix it. Freshness is never stored (SRS BR-05).
 */
public class FreshnessClassifier {

    // TODO: replace with "import static medfinder.config.AppConfig.FRESH_HOURS / AGING_HOURS"
    // once P2 delivers AppConfig (21 Nov). Kept here so the project compiles before then.
    private static final long FRESH_HOURS = 12;
    private static final long AGING_HOURS = 48;

    private FreshnessClassifier() {
    }

    /**
     * @param lastConfirmedAt when the stock was last confirmed
     * @param now             the current time
     * @return FRESH if the age is at most 12 hours, AGING if at most 48 hours, otherwise STALE
     * @throws ValidationException if a time is null
     */
    public static Freshness classify(Instant lastConfirmedAt, Instant now) {
        if (lastConfirmedAt == null || now == null) {
            throw new ValidationException("Both times are needed to work out freshness.");
        }
        Duration age = Duration.between(lastConfirmedAt, now);
        if (age.compareTo(Duration.ofHours(FRESH_HOURS)) <= 0) {
            return Freshness.FRESH;
        }
        if (age.compareTo(Duration.ofHours(AGING_HOURS)) <= 0) {
            return Freshness.AGING;
        }
        return Freshness.STALE;
    }
}
