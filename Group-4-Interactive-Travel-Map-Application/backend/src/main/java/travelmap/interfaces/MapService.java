package travelmap.interfaces;

import java.util.List;

import travelmap.map.DistanceResult;
import travelmap.model.Pin;

/**
 * MapService — service interface for FR8 map and distance operations.
 *
 * Defines the contract between MapController (HTTP layer) and MapAPIClient
 * (business logic layer). MapController depends on this interface, not on
 * MapAPIClient directly — satisfying DIP (Dependency Inversion Principle).
 *
 * Replaces the original skeleton methods (renderMap, calculateDistance) with
 * the three FR8 distance modes required by the SRS. renderMap is removed
 * because map rendering is handled entirely on the React/Leaflet side —
 * the backend never renders a map. calculateDistance is replaced by the
 * three typed methods below which carry full context (unit preference,
 * skipped pin reporting) required by the SRS.
 *
 * NFR6 Maintainability — MapAPIClient can be swapped for a routing-API
 * implementation (e.g. Google Maps Directions) without changing MapController.
 *
 * SRS coverage:
 *   FR8  — all three distance modes
 *   FR14 — unit parameter honours user measurement preference
 */
public interface MapService {

    /**
     * FR8 mode 1 — compute great-circle distance between two explicit points.
     *
     * @param lat1 latitude of point A in degrees, [-90, 90]
     * @param lon1 longitude of point A in degrees, [-180, 180]
     * @param lat2 latitude of point B in degrees, [-90, 90]
     * @param lon2 longitude of point B in degrees, [-180, 180]
     * @param unit requested unit, "km" or "mi" — defaults to "km" if null or other
     * @return DistanceResult with both km and mi values and the preferred field set
     * @throws IllegalArgumentException if any coordinate is null or out of range
     */
    DistanceResult getDistanceBetween(Double lat1, Double lon1,
                                      Double lat2, Double lon2,
                                      String unit);

    /**
     * FR8 mode 2 — total distance of one trip, pins walked in visit-date order.
     *
     * Pins with null coordinates are skipped and their ids reported in the
     * result. Empty trip or single usable pin returns distance 0.
     *
     * @param tripId database id of the trip
     * @param unit   requested unit, "km" or "mi" — defaults to "km" if null or other
     * @return DistanceResult with total distance and any skipped pin ids
     * @throws IllegalArgumentException if no trip exists with the given id
     */
    DistanceResult getTripDistance(Long tripId, String unit);

    /**
     * FR8 mode 3 — accumulated travel distance across all trips for a user.
     *
     * Skipped pin ids from every trip are aggregated into a single list.
     * Returns distance 0 if the user has no trips.
     *
     * @param ownerId id of the user whose trips should be summed
     * @param unit    requested unit, "km" or "mi" — defaults to "km" if null or other
     * @return DistanceResult with total accumulated distance and any skipped pin ids
     * @throws IllegalArgumentException if ownerId is null or blank
     */
    DistanceResult getTotalDistance(String ownerId, String unit);

    /**
     * FR6 — render the map with the given pins and routes.
     *
     * Retained from the original skeleton for FR6 compatibility. Map rendering
     * is handled on the React/Leaflet side — this method is a hook for any
     * future server-side map processing (e.g. static map image generation).
     *
     * @param pins   list of pins to display on the map
     * @param routes list of route objects between pins
     */
    void renderMap(List<Pin> pins, List<Object> routes);
}
