package travelmap.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import travelmap.interfaces.IMapController;
import travelmap.interfaces.MapService;
import travelmap.map.DistanceResult;

/**
 * MapController — Controller layer (MVC), FR8 distance API.
 *
 * Handles all incoming HTTP requests for distance calculations.
 * Delegates immediately to MapService — no business logic lives here.
 * Mirrors PinController and TripController in structure and style.
 *
 * Architecture role:
 *   - Receives HTTP, delegates to MapService, returns JSON.
 *   - MapService (interface) is injected — not MapAPIClient directly.
 *     This satisfies DIP: controller depends on the abstraction, not
 *     the concrete implementation.
 *   - No repository access, no Haversine math, no sorting — all of
 *     that lives in MapAPIClient (the MapService implementation).
 *
 * SRS coverage:
 *   FR8  — three distance endpoints: pin-to-pin, trip total, accumulated
 *   FR14 — unit preference honoured via ?unit=km|mi query parameter
 *
 * NFR coverage:
 *   NFR2 — delegates to MapAPIClient which uses pure Haversine math,
 *           completes well within the 2.5-second requirement
 *   NFR4 — getTotalDistance scoped to ownerId; full enforcement pending
 *           Wilson's AuthController merge
 *
 * CORS:
 *   @CrossOrigin allows React on localhost:3000 to call this API.
 *   TODO (deployment): replace with WebMvcConfigurer CORS bean before
 *   production — same TODO flagged on PinController and TripController.
 *
 * TODO (Wilson — NFR4):
 *   Once AuthController merges, replace the ownerId @RequestParam in
 *   getTotalDistance with the authenticated user from Spring Security.
 */
@RestController
@RequestMapping("/api/map/distance")
@CrossOrigin(origins = "http://localhost:3000")
public class MapController implements IMapController {

    private final MapService mapService;

    /**
     * Constructor injection — MapService provided by Spring on startup.
     * Spring injects MapAPIClient (the @Component implementation of MapService)
     * automatically. Constructor injection keeps MapControllerTest clean:
     *   MapController controller = new MapController(mockMapService);
     *
     * @param mapService the service handling all distance business logic
     */
    @Autowired
    public MapController(MapService mapService) {
        this.mapService = mapService;
    }

    /**
     * FR8 mode 1 — pin-to-pin great-circle distance.
     *
     * Stateless: delegates directly to MapService.getDistanceBetween.
     * No DB access in this method — pure coordinate math in the service.
     *
     * Returns HTTP 400 if any coordinate is null or out of range.
     * React's Leaflet map click always produces valid coords, but
     * defensive validation is required per NFR3 reliability.
     *
     * Example request:
     *   GET /api/map/distance?lat1=40.71&lon1=-74.00&lat2=34.05&lon2=-118.24&unit=km
     *
     * @param lat1 latitude of point A in degrees, [-90, 90]
     * @param lon1 longitude of point A in degrees, [-180, 180]
     * @param lat2 latitude of point B in degrees, [-90, 90]
     * @param lon2 longitude of point B in degrees, [-180, 180]
     * @param unit "km" or "mi" — defaults to "km"
     * @return HTTP 200 with DistanceResult, HTTP 400 if any coordinate is invalid
     */
    @Override
    @GetMapping
    public ResponseEntity<DistanceResult> getDistanceBetween(
            @RequestParam Double lat1,
            @RequestParam Double lon1,
            @RequestParam Double lat2,
            @RequestParam Double lon2,
            @RequestParam(required = false, defaultValue = "km") String unit) {
        try {
            return ResponseEntity.ok(
                    mapService.getDistanceBetween(lat1, lon1, lat2, lon2, unit));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * FR8 mode 2 — total distance of a single trip in chronological order.
     *
     * Delegates to MapService.getTripDistance. The service verifies the
     * trip exists, fetches pins, sorts by visitDate, and sums distances.
     * Pins with null coordinates are skipped and reported in skippedPinIds.
     *
     * Returns HTTP 404 if no trip exists with the given id.
     * Returns HTTP 200 with distance 0 for empty or single-pin trips.
     *
     * Example request:
     *   GET /api/map/distance/trip/1?unit=mi
     *
     * @param tripId database id of the trip
     * @param unit   "km" or "mi" — defaults to "km"
     * @return HTTP 200 with DistanceResult, HTTP 404 if trip not found
     */
    @Override
    @GetMapping("/trip/{tripId}")
    public ResponseEntity<DistanceResult> getTripDistance(
            @PathVariable Long tripId,
            @RequestParam(required = false, defaultValue = "km") String unit) {
        try {
            return ResponseEntity.ok(
                    mapService.getTripDistance(tripId, unit));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * FR8 mode 3 — accumulated travel distance across all of a user's trips.
     *
     * Delegates to MapService.getTotalDistance. The service validates ownerId,
     * iterates all user trips, sums distances, and aggregates skipped pin ids.
     *
     * Returns HTTP 400 if ownerId is missing or blank.
     * Returns HTTP 200 with distance 0 if the user has no trips.
     *
     * Example request:
     *   GET /api/map/distance/total?ownerId=user-1&unit=km
     *
     * @param ownerId id of the user whose trips should be summed
     * @param unit    "km" or "mi" — defaults to "km"
     * @return HTTP 200 with DistanceResult, HTTP 400 if ownerId is missing
     */
    @Override
    @GetMapping("/total")
    public ResponseEntity<DistanceResult> getTotalDistance(
            @RequestParam String ownerId,
            @RequestParam(required = false, defaultValue = "km") String unit) {
        try {
            return ResponseEntity.ok(
                    mapService.getTotalDistance(ownerId, unit));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}