package travelmap.map;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import travelmap.interfaces.MapService;
import travelmap.model.Pin;
import travelmap.model.Trip;
import travelmap.repository.PinRepository;
import travelmap.repository.TripRepository;

/**
 * MapAPIClient — Service layer (MVC), FR8 distance business logic.
 *
 * Implements MapService — the single class responsible for all distance
 * computation and map-related business logic. MapController delegates here
 * immediately and does nothing else. No HTTP types, no ResponseEntity,
 * no Spring MVC annotations live in this class.
 *
 * Architecture role:
 *   - Sits between MapController (HTTP) and the repositories (DB).
 *   - Mirrors PinService and TripService in role and structure.
 *   - PinRepository and TripRepository are injected via constructor —
 *     this is the only layer that accesses them for map operations.
 *   - DistanceCalculator is a pure-math utility called from here —
 *     no math lives in the controller or the repositories.
 *
 * Why MapAPIClient and not MapService as the class name:
 *   MapAPIClient was the name given in the original UML skeleton.
 *   MapService is the interface it implements. The name is retained to
 *   avoid breaking the UML diagram before final submission. Rename to
 *   MapServiceImpl if desired during the final cleanup sprint.
 *
 * SRS coverage:
 *   FR8  — getDistanceBetween, getTripDistance, getTotalDistance
 *   FR5  — pins walked in chronological visitDate order within a trip
 *   FR14 — unit parameter propagated to DistanceResult for user preference
 *   FR6  — renderMap hook retained for future server-side map processing
 *
 * NFR coverage:
 *   NFR2 — Haversine is microseconds; all DB calls are single-table reads
 *   NFR4 — getTotalDistance scoped to ownerId; full auth enforcement
 *           pending Wilson's AuthController merge
 *   NFR6 — implements MapService interface; swap implementation without
 *           touching MapController
 *
 * TODO (Wilson — NFR4):
 *   getTotalDistance currently accepts any ownerId as a parameter.
 *   Once AuthController merges, verify the requesting user matches
 *   ownerId before returning data.
 *
 * TODO (final sprint):
 *   Move PinService and TripService from travelmap.repository to
 *   travelmap.service. Move this class at the same time for consistency.
 *   Update package declarations and imports accordingly.
 */
@Component
public class MapAPIClient implements MapService {

    private final PinRepository pinRepository;
    private final TripRepository tripRepository;

    /**
     * Constructor injection — both repositories provided by Spring on startup.
     * Constructor injection makes MapAPIClientTest clean: pass mocks directly
     * via new MapAPIClient(mockPinRepo, mockTripRepo).
     *
     * @param pinRepository  JPA repository for per-trip pin queries
     * @param tripRepository JPA repository for trip lookups and owner queries
     */
    @Autowired
    public MapAPIClient(PinRepository pinRepository, TripRepository tripRepository) {
        this.pinRepository = pinRepository;
        this.tripRepository = tripRepository;
    }

    /**
     * FR8 mode 1 — compute great-circle distance between two explicit points.
     *
     * Purely delegates to DistanceCalculator — no DB access needed.
     * IllegalArgumentException from DistanceCalculator propagates up to
     * MapController which catches it and returns HTTP 400.
     *
     * @param lat1 latitude of point A in degrees, [-90, 90]
     * @param lon1 longitude of point A in degrees, [-180, 180]
     * @param lat2 latitude of point B in degrees, [-90, 90]
     * @param lon2 longitude of point B in degrees, [-180, 180]
     * @param unit requested unit, "km" or "mi" — defaults to "km" if null or other
     * @return DistanceResult with both km and mi values and preferred field set
     * @throws IllegalArgumentException if any coordinate is null or out of range
     */
    @Override
    public DistanceResult getDistanceBetween(Double lat1, Double lon1,
                                              Double lat2, Double lon2,
                                              String unit) {
        double km = DistanceCalculator.haversineKm(lat1, lon1, lat2, lon2);
        return new DistanceResult(km, unit, null);
    }

    /**
     * FR8 mode 2 — total distance of one trip, pins walked in visit-date order.
     *
     * Loads pins via PinRepository.findByTripId because Trip.pins is
     * @Transient and does not hydrate from the DB. Pins are sorted by
     * visitDate (FR5 chronological requirement) with nulls last. Pins
     * with null coordinates are skipped and their ids reported in the result.
     *
     * @param tripId database id of the trip
     * @param unit   requested unit, "km" or "mi" — defaults to "km" if null or other
     * @return DistanceResult with total distance and any skipped pin ids
     * @throws IllegalArgumentException if no trip exists with the given id
     */
    @Override
    public DistanceResult getTripDistance(Long tripId, String unit) {
        tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found: " + tripId));

        List<Pin> pins = pinRepository.findByTripId(tripId);
        TripDistanceComputation result = computeTripDistance(pins);
        return new DistanceResult(result.km, unit, result.skippedPinIds);
    }

    /**
     * FR8 mode 3 — accumulated travel distance across all trips for a user.
     *
     * For each trip owned by the user, computes the trip distance the same
     * way as getTripDistance, then sums the results. Skipped pin ids are
     * aggregated across all trips into a single list.
     *
     * @param ownerId id of the user whose trips should be summed
     * @param unit    requested unit, "km" or "mi" — defaults to "km" if null or other
     * @return DistanceResult with total accumulated distance and any skipped pin ids
     * @throws IllegalArgumentException if ownerId is null or blank
     */
    @Override
    public DistanceResult getTotalDistance(String ownerId, String unit) {
        if (ownerId == null || ownerId.trim().isEmpty()) {
            throw new IllegalArgumentException("ownerId must not be null or blank");
        }

        List<Trip> trips = tripRepository.findByOwnerId(ownerId);
        double totalKm = 0.0;
        List<Long> skippedPinIds = new ArrayList<>();

        for (Trip trip : trips) {
            List<Pin> pins = pinRepository.findByTripId(trip.getId());
            TripDistanceComputation result = computeTripDistance(pins);
            totalKm += result.km;
            skippedPinIds.addAll(result.skippedPinIds);
        }

        return new DistanceResult(totalKm, unit, skippedPinIds);
    }

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
    @Override
    public void renderMap(List<Pin> pins, List<Object> routes) {
        // No-op — map rendering is handled by React/Leaflet on the frontend.
        // Retained for FR6 interface compliance and future server-side use.
    }

    /**
     * Compute the great-circle distance for a list of pins belonging to one trip.
     *
     * Sorts by visitDate (nulls last), partitions into usable pins (both
     * coordinates present) and skipped pins (either coordinate null), then
     * sums the Haversine distance between each consecutive pair of usable pins.
     *
     * Empty input or a single usable pin yields 0 distance — distance is
     * undefined when there are fewer than two waypoints.
     *
     * @param pins unsorted pins for one trip — this method sorts them
     * @return TripDistanceComputation with total km and list of skipped pin ids
     */
    private TripDistanceComputation computeTripDistance(List<Pin> pins) {
        List<Long> skipped = new ArrayList<>();
        List<Pin> usable = new ArrayList<>();

        List<Pin> sorted = new ArrayList<>(pins);
        sorted.sort(Comparator.comparing(
                Pin::getVisitDate,
                Comparator.nullsLast(Comparator.naturalOrder())));

        for (Pin pin : sorted) {
            if (pin.getLatitude() == null || pin.getLongitude() == null) {
                skipped.add(pin.getId());
            } else {
                usable.add(pin);
            }
        }

        double km = 0.0;
        for (int i = 1; i < usable.size(); i++) {
            Pin a = usable.get(i - 1);
            Pin b = usable.get(i);
            km += DistanceCalculator.haversineKm(
                    a.getLatitude(), a.getLongitude(),
                    b.getLatitude(), b.getLongitude());
        }

        return new TripDistanceComputation(km, skipped);
    }

    /**
     * Internal holder so computeTripDistance can return two values cleanly.
     *
     * @param km           total great-circle distance in kilometres
     * @param skippedPinIds ids of pins skipped due to null coordinates
     */
    private static final class TripDistanceComputation {
        final double km;
        final List<Long> skippedPinIds;

        TripDistanceComputation(double km, List<Long> skippedPinIds) {
            this.km = km;
            this.skippedPinIds = skippedPinIds;
        }
    }
}