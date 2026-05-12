package travelmap.interfaces;

import org.springframework.http.ResponseEntity;

import travelmap.map.DistanceResult;

/**
 * IMapController — interface contract for the FR8 distance API.
 *
 * Defines the three distance modes required by SRS FR8: pin-to-pin,
 * total trip distance, and accumulated travel distance for a user.
 *
 * Existence of this interface satisfies:
 *   - DIP (Dependency Inversion) — higher-level code can depend on this
 *     contract instead of MapController's concrete class.
 *   - ISP (Interface Segregation) — clients of MapController are not
 *     forced to know about pin or trip endpoints defined elsewhere.
 *
 * Mirrors the IPinController and ITripController pattern used throughout
 * this project. NFR6 Maintainability — implementation can be swapped
 * (e.g. replacing Haversine with a routing API) without changing callers.
 */
public interface IMapController {

    /**
     * FR8 — pin-to-pin great-circle distance.
     *
     * @param lat1 latitude of point A in degrees, [-90, 90]
     * @param lon1 longitude of point A in degrees, [-180, 180]
     * @param lat2 latitude of point B in degrees, [-90, 90]
     * @param lon2 longitude of point B in degrees, [-180, 180]
     * @param unit requested unit, "km" or "mi" — defaults to "km" when absent or other
     * @return HTTP 200 with DistanceResult, HTTP 400 if any coordinate is invalid
     */
    ResponseEntity<DistanceResult> getDistanceBetween(
            Double lat1, Double lon1, Double lat2, Double lon2, String unit);

    /**
     * FR8 — total distance of one trip, summed across pins in visit-date order.
     *
     * Pins with null coordinates are skipped and reported in skippedPinIds.
     * Empty trip or single usable pin returns distance 0.
     *
     * @param tripId database id of the trip
     * @param unit   requested unit, "km" or "mi" — defaults to "km" when absent or other
     * @return HTTP 200 with DistanceResult, HTTP 404 if trip does not exist
     */
    ResponseEntity<DistanceResult> getTripDistance(Long tripId, String unit);

    /**
     * FR8 — accumulated travel distance for a user across all their trips.
     *
     * Skipped pin ids from every trip are aggregated into a single response
     * list. Returns distance 0 if the user has no trips.
     *
     * Once Wilson's auth lands, ownerId will be replaced by the authenticated
     * user from Spring Security. Until then it is a query parameter so the
     * endpoint is testable without auth.
     *
     * @param ownerId id of the user whose trips should be summed
     * @param unit    requested unit, "km" or "mi" — defaults to "km" when absent or other
     * @return HTTP 200 with DistanceResult, HTTP 400 if ownerId is missing or blank
     */
    ResponseEntity<DistanceResult> getTotalDistance(String ownerId, String unit);
}
