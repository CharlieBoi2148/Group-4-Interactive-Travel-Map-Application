package travelmap.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * DistanceResult — DTO returned by every MapController distance endpoint.
 *
 * Returns both km and mi values plus a "preferred" field that mirrors
 * whichever unit the caller requested via the ?unit query parameter.
 * Frontend can read both and choose at render time (per FR14 user
 * preference) or use the preferred field directly for the simple case.
 *
 * skippedPinIds is populated for the trip-distance and total-distance
 * endpoints when a pin is encountered with null coordinates — the pin
 * is skipped from the calculation and its id is reported back so the
 * UI can flag the data integrity issue. Empty list for the pin-to-pin
 * endpoint where no pins are looked up from the database.
 *
 * Jackson serializes this directly to JSON via the public getters.
 *
 * SRS coverage:
 *   FR8  — distanceKm, distanceMi, skippedPinIds
 *   FR14 — preferred and unit honour the user's measurement preference
 */
public class DistanceResult {

    private final double distanceKm;
    private final double distanceMi;
    private final double preferred;
    private final String unit;
    private final List<Long> skippedPinIds;

    /**
     * @param distanceKm    total distance in kilometres
     * @param unit          requested unit — "km" or "mi", defaults to "km" if null or other
     * @param skippedPinIds ids of pins skipped due to null coordinates, may be empty or null
     */
    public DistanceResult(double distanceKm, String unit, List<Long> skippedPinIds) {
        this.distanceKm = distanceKm;
        this.distanceMi = DistanceCalculator.kmToMiles(distanceKm);
        this.unit = "mi".equalsIgnoreCase(unit) ? "mi" : "km";
        this.preferred = "mi".equals(this.unit) ? this.distanceMi : this.distanceKm;
        this.skippedPinIds = skippedPinIds == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(skippedPinIds));
    }

    /** @return distance in kilometres, always non-negative */
    public double getDistanceKm() { return distanceKm; }

    /** @return distance in miles, always non-negative */
    public double getDistanceMi() { return distanceMi; }

    /** @return the requested unit's value (the user's preference per FR14) */
    public double getPreferred() { return preferred; }

    /** @return either "km" or "mi" — the unit selected for the preferred field */
    public String getUnit() { return unit; }

    /** @return ids of pins skipped due to null coordinates, never null */
    public List<Long> getSkippedPinIds() { return skippedPinIds; }
}
