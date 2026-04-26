package travelmap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import travelmap.map.DistanceCalculator;
import travelmap.map.DistanceResult;
import travelmap.map.MapAPIClient;
import travelmap.model.Pin;
import travelmap.model.Trip;
import travelmap.repository.PinRepository;
import travelmap.repository.TripRepository;

/**
 * MapAPIClientTest — Unit tests for MapAPIClient (Service layer, FR8).
 *
 * Tests business logic in isolation — no Spring context, no H2 database.
 * PinRepository and TripRepository are mocked with Mockito; MapAPIClient is
 * constructed directly via new MapAPIClient(mockPinRepo, mockTripRepo).
 * This is why constructor injection was used in that class.
 *
 * Input partitioning strategy:
 *   getDistanceBetween — unit "km", unit "mi", null coord, out-of-range coord
 *   getTripDistance    — trip not found, 0 pins, 1 pin, 2 pins, 3 pins,
 *                        null latitude, null longitude,
 *                        scrambled date order (sort verified with asymmetric path),
 *                        null visitDate (sorts to end — usable, not skipped)
 *   getTotalDistance   — null ownerId, whitespace-only ownerId, no trips,
 *                        one trip, multiple trips, skipped pins aggregated across trips
 *
 * SRS coverage:
 *   FR8  — all three distance modes and their error paths
 *   FR5  — pins walked in chronological visitDate order (sort partition tests)
 *   FR14 — unit parameter propagated correctly to DistanceResult
 */
@ExtendWith(MockitoExtension.class)
class MapAPIClientTest {

    @Mock
    private PinRepository pinRepository;

    @Mock
    private TripRepository tripRepository;

    private MapAPIClient mapAPIClient;

    /** 1-metre tolerance — tight enough for same-input Haversine comparisons. */
    private static final double TOLERANCE = 0.001;

    @BeforeEach
    void setUp() {
        mapAPIClient = new MapAPIClient(pinRepository, tripRepository);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Pin buildPin(Long id, Double lat, Double lon, LocalDate visitDate) {
        Pin pin = new Pin();
        pin.setId(id);
        pin.setLatitude(lat);
        pin.setLongitude(lon);
        pin.setVisitDate(visitDate);
        pin.setLocationName("Location " + id);
        return pin;
    }

    private Trip buildTrip(Long id, String ownerId) {
        Trip trip = new Trip();
        trip.setId(id);
        trip.setOwnerId(ownerId);
        return trip;
    }

    // ── getDistanceBetween ────────────────────────────────────────────────────

    @Test
    void getDistanceBetween_validCoords_unitKm_returnsDistanceResult_FR8() {
        // Arrange — London to Paris, known great-circle distance
        double londonLat = 51.5074, londonLon = -0.1278;
        double parisLat  = 48.8566, parisLon  =  2.3522;

        // Act
        DistanceResult result = mapAPIClient.getDistanceBetween(
                londonLat, londonLon, parisLat, parisLon, "km");

        // Assert — result must match DistanceCalculator's own output for the same inputs
        double expectedKm = DistanceCalculator.haversineKm(londonLat, londonLon, parisLat, parisLon);
        assertEquals(expectedKm, result.getDistanceKm(), TOLERANCE);
        assertEquals("km", result.getUnit());
        assertEquals(expectedKm, result.getPreferred(), TOLERANCE);
        assertTrue(result.getSkippedPinIds().isEmpty());
    }

    @Test
    void getDistanceBetween_validCoords_unitMi_returnsMiAsPreferred_FR8_FR14() {
        // Arrange
        double londonLat = 51.5074, londonLon = -0.1278;
        double parisLat  = 48.8566, parisLon  =  2.3522;

        // Act
        DistanceResult result = mapAPIClient.getDistanceBetween(
                londonLat, londonLon, parisLat, parisLon, "mi");

        // Assert — preferred must be the mi value, not the km value (FR14)
        double expectedMi = DistanceCalculator.kmToMiles(
                DistanceCalculator.haversineKm(londonLat, londonLon, parisLat, parisLon));
        assertEquals("mi", result.getUnit());
        assertEquals(expectedMi, result.getPreferred(), TOLERANCE);
    }

    @Test
    void getDistanceBetween_nullCoordinate_throwsIllegalArgumentException_FR8() {
        // IllegalArgumentException propagates from DistanceCalculator — not caught by MapAPIClient
        assertThrows(IllegalArgumentException.class,
                () -> mapAPIClient.getDistanceBetween(null, 0.0, 0.0, 0.0, "km"));
    }

    @Test
    void getDistanceBetween_outOfRangeLatitude_throwsIllegalArgumentException_FR8() {
        // latitude 91 exceeds the [-90, 90] domain
        assertThrows(IllegalArgumentException.class,
                () -> mapAPIClient.getDistanceBetween(91.0, 0.0, 0.0, 0.0, "km"));
    }

    // ── getTripDistance ───────────────────────────────────────────────────────

    @Test
    void getTripDistance_tripNotFound_throwsIllegalArgumentException_FR8() {
        // Arrange
        when(tripRepository.findById(99L)).thenReturn(Optional.empty());

        // Act + Assert — MapController catches this and returns HTTP 404
        assertThrows(IllegalArgumentException.class,
                () -> mapAPIClient.getTripDistance(99L, "km"));
    }

    @Test
    void getTripDistance_emptyPinList_returnsZeroDistance_FR8() {
        // Arrange — trip exists but has no pins assigned
        Trip trip = buildTrip(1L, "user1");
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(pinRepository.findByTripId(1L)).thenReturn(List.of());

        // Act
        DistanceResult result = mapAPIClient.getTripDistance(1L, "km");

        // Assert
        assertEquals(0.0, result.getDistanceKm(), 0.0);
        assertTrue(result.getSkippedPinIds().isEmpty());
    }

    @Test
    void getTripDistance_singlePin_returnsZeroDistance_FR8() {
        // Arrange — 1 usable pin: distance undefined when fewer than 2 waypoints
        Trip trip = buildTrip(1L, "user1");
        Pin pin = buildPin(1L, 51.5074, -0.1278, LocalDate.of(2024, 1, 1));
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(pinRepository.findByTripId(1L)).thenReturn(List.of(pin));

        // Act
        DistanceResult result = mapAPIClient.getTripDistance(1L, "km");

        // Assert
        assertEquals(0.0, result.getDistanceKm(), 0.0);
        assertTrue(result.getSkippedPinIds().isEmpty());
    }

    @Test
    void getTripDistance_twoPins_returnsHaversineDistance_FR8() {
        // Arrange — London to Paris
        Trip trip = buildTrip(1L, "user1");
        Pin london = buildPin(1L, 51.5074, -0.1278, LocalDate.of(2024, 1, 1));
        Pin paris  = buildPin(2L, 48.8566,  2.3522, LocalDate.of(2024, 1, 2));
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(pinRepository.findByTripId(1L)).thenReturn(List.of(london, paris));

        // Act
        DistanceResult result = mapAPIClient.getTripDistance(1L, "km");

        // Assert
        double expected = DistanceCalculator.haversineKm(51.5074, -0.1278, 48.8566, 2.3522);
        assertEquals(expected, result.getDistanceKm(), TOLERANCE);
        assertTrue(result.getSkippedPinIds().isEmpty());
    }

    @Test
    void getTripDistance_threePins_returnsSumOfConsecutiveDistances_FR8() {
        // Arrange — London → Paris → Berlin (consecutive pairs, not direct London→Berlin)
        Trip trip = buildTrip(1L, "user1");
        Pin london = buildPin(1L, 51.5074, -0.1278, LocalDate.of(2024, 1, 1));
        Pin paris  = buildPin(2L, 48.8566,  2.3522, LocalDate.of(2024, 1, 2));
        Pin berlin = buildPin(3L, 52.5200, 13.4050, LocalDate.of(2024, 1, 3));
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(pinRepository.findByTripId(1L)).thenReturn(List.of(london, paris, berlin));

        // Act
        DistanceResult result = mapAPIClient.getTripDistance(1L, "km");

        // Assert
        double londonParis = DistanceCalculator.haversineKm(51.5074, -0.1278, 48.8566, 2.3522);
        double parisBerlin = DistanceCalculator.haversineKm(48.8566, 2.3522, 52.5200, 13.4050);
        assertEquals(londonParis + parisBerlin, result.getDistanceKm(), TOLERANCE);
        assertTrue(result.getSkippedPinIds().isEmpty());
    }

    @Test
    void getTripDistance_pinWithNullLatitude_isSkipped_FR8() {
        // Arrange — pin2 has null latitude: must appear in skippedPinIds, excluded from computation
        Trip trip = buildTrip(1L, "user1");
        Pin pin1 = buildPin(1L, 51.5074, -0.1278, LocalDate.of(2024, 1, 1));
        Pin pin2 = buildPin(2L, null,     2.3522, LocalDate.of(2024, 1, 2)); // null lat → skipped
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(pinRepository.findByTripId(1L)).thenReturn(List.of(pin1, pin2));

        // Act
        DistanceResult result = mapAPIClient.getTripDistance(1L, "km");

        // Assert — only pin1 is usable; 1 usable pin → 0 distance
        assertEquals(0.0, result.getDistanceKm(), 0.0);
        assertEquals(List.of(2L), result.getSkippedPinIds());
    }

    @Test
    void getTripDistance_pinWithNullLongitude_isSkipped_FR8() {
        // Arrange — pin2 has null longitude: same partition as null latitude
        Trip trip = buildTrip(1L, "user1");
        Pin pin1 = buildPin(1L, 51.5074, -0.1278, LocalDate.of(2024, 1, 1));
        Pin pin2 = buildPin(2L, 48.8566,  null,   LocalDate.of(2024, 1, 2)); // null lon → skipped
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(pinRepository.findByTripId(1L)).thenReturn(List.of(pin1, pin2));

        // Act
        DistanceResult result = mapAPIClient.getTripDistance(1L, "km");

        // Assert
        assertEquals(0.0, result.getDistanceKm(), 0.0);
        assertEquals(List.of(2L), result.getSkippedPinIds());
    }

    @Test
    void getTripDistance_pinsScrambled_sortedByVisitDateCorrectly_FR8() {
        // Arrange — three pins given in non-chronological input order.
        //
        // Points are chosen so the correct chronological path (NY→Paris→London ≈ 6180 km)
        // is measurably different from the unsorted input path (London→NY→Paris ≈ 11412 km),
        // proving that computeTripDistance applies the visitDate sort before walking pairs.
        //
        // Chronological order: newYork(01-01) → paris(01-02) → london(01-03)
        // Input order given:   london(01-03) → newYork(01-01) → paris(01-02)   ← scrambled
        Trip trip = buildTrip(1L, "user1");
        Pin newYork = buildPin(1L, 40.7128, -74.0060, LocalDate.of(2024, 1, 1)); // oldest → first
        Pin paris   = buildPin(2L, 48.8566,   2.3522, LocalDate.of(2024, 1, 2));
        Pin london  = buildPin(3L, 51.5074,  -0.1278, LocalDate.of(2024, 1, 3)); // newest → last
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(pinRepository.findByTripId(1L)).thenReturn(List.of(london, newYork, paris));

        // Act
        DistanceResult result = mapAPIClient.getTripDistance(1L, "km");

        // Assert — sorted path: NY → Paris → London (≈ 6180 km)
        // Wrong unsorted path (London→NY→Paris) would give ≈ 11412 km — distinguishably different
        double nyToParis    = DistanceCalculator.haversineKm(40.7128, -74.0060, 48.8566, 2.3522);
        double parisToLon   = DistanceCalculator.haversineKm(48.8566, 2.3522, 51.5074, -0.1278);
        assertEquals(nyToParis + parisToLon, result.getDistanceKm(), TOLERANCE);
    }

    @Test
    void getTripDistance_pinWithNullVisitDate_treatedAsUsableAndSortsToEnd_FR8() {
        // Arrange — null visitDate must sort to the end (nullsLast), NOT be skipped.
        // The pin still participates in distance computation if its coordinates are valid.
        //
        // Scrambled input: nullDate(Berlin coords), paris, london
        // Expected sort:   london(01-01) → paris(01-02) → nullDate(last, Berlin coords)
        Trip trip = buildTrip(1L, "user1");
        Pin london   = buildPin(1L, 51.5074, -0.1278, LocalDate.of(2024, 1, 1));
        Pin paris    = buildPin(2L, 48.8566,  2.3522, LocalDate.of(2024, 1, 2));
        Pin nullDate = buildPin(3L, 52.5200, 13.4050, null); // null visitDate → sorts to end
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(pinRepository.findByTripId(1L)).thenReturn(List.of(nullDate, paris, london));

        // Act
        DistanceResult result = mapAPIClient.getTripDistance(1L, "km");

        // Assert — distance: London→Paris + Paris→nullDate(Berlin coords)
        double londonParis = DistanceCalculator.haversineKm(51.5074, -0.1278, 48.8566, 2.3522);
        double parisBerlin = DistanceCalculator.haversineKm(48.8566, 2.3522, 52.5200, 13.4050);
        assertEquals(londonParis + parisBerlin, result.getDistanceKm(), TOLERANCE);
        assertTrue(result.getSkippedPinIds().isEmpty()); // null visitDate → usable, NOT skipped
    }

    // ── getTotalDistance ──────────────────────────────────────────────────────

    @Test
    void getTotalDistance_nullOwnerId_throwsIllegalArgumentException_FR8() {
        assertThrows(IllegalArgumentException.class,
                () -> mapAPIClient.getTotalDistance(null, "km"));
    }

    @Test
    void getTotalDistance_blankOwnerId_throwsIllegalArgumentException_FR8() {
        // Whitespace-only string must fail the trim().isEmpty() check
        assertThrows(IllegalArgumentException.class,
                () -> mapAPIClient.getTotalDistance("   ", "km"));
    }

    @Test
    void getTotalDistance_noTrips_returnsZeroDistance_FR8() {
        // Arrange
        when(tripRepository.findByOwnerId("user1")).thenReturn(List.of());

        // Act
        DistanceResult result = mapAPIClient.getTotalDistance("user1", "km");

        // Assert — no trips → no distance, no skipped pins
        assertEquals(0.0, result.getDistanceKm(), 0.0);
        assertTrue(result.getSkippedPinIds().isEmpty());
    }

    @Test
    void getTotalDistance_oneTrip_returnsTripDistance_FR8() {
        // Arrange — one trip with two pins: London → Paris
        Trip trip = buildTrip(1L, "user1");
        Pin london = buildPin(1L, 51.5074, -0.1278, LocalDate.of(2024, 1, 1));
        Pin paris  = buildPin(2L, 48.8566,  2.3522, LocalDate.of(2024, 1, 2));
        when(tripRepository.findByOwnerId("user1")).thenReturn(List.of(trip));
        when(pinRepository.findByTripId(1L)).thenReturn(List.of(london, paris));

        // Act
        DistanceResult result = mapAPIClient.getTotalDistance("user1", "km");

        // Assert
        double expected = DistanceCalculator.haversineKm(51.5074, -0.1278, 48.8566, 2.3522);
        assertEquals(expected, result.getDistanceKm(), TOLERANCE);
        assertTrue(result.getSkippedPinIds().isEmpty());
    }

    @Test
    void getTotalDistance_multipleTrips_sumsDistancesAcrossTrips_FR8() {
        // Arrange — two trips: trip1 = London→Paris, trip2 = Paris→Berlin
        Trip trip1 = buildTrip(1L, "user1");
        Trip trip2 = buildTrip(2L, "user1");
        Pin london = buildPin(1L, 51.5074, -0.1278, LocalDate.of(2024, 1, 1));
        Pin paris1 = buildPin(2L, 48.8566,  2.3522, LocalDate.of(2024, 1, 2));
        Pin paris2 = buildPin(3L, 48.8566,  2.3522, LocalDate.of(2024, 2, 1));
        Pin berlin = buildPin(4L, 52.5200, 13.4050, LocalDate.of(2024, 2, 2));
        when(tripRepository.findByOwnerId("user1")).thenReturn(List.of(trip1, trip2));
        when(pinRepository.findByTripId(1L)).thenReturn(List.of(london, paris1));
        when(pinRepository.findByTripId(2L)).thenReturn(List.of(paris2, berlin));

        // Act
        DistanceResult result = mapAPIClient.getTotalDistance("user1", "km");

        // Assert — total = trip1 distance + trip2 distance
        double trip1Km = DistanceCalculator.haversineKm(51.5074, -0.1278, 48.8566, 2.3522);
        double trip2Km = DistanceCalculator.haversineKm(48.8566, 2.3522, 52.5200, 13.4050);
        assertEquals(trip1Km + trip2Km, result.getDistanceKm(), TOLERANCE);
        assertTrue(result.getSkippedPinIds().isEmpty());
    }

    @Test
    void getTotalDistance_skippedPinsAggregatedAcrossTrips_FR8() {
        // Arrange — each trip has one skipped pin (null coord) and one usable pin.
        // With only 1 usable pin per trip, each trip distance is 0.
        // Both skipped ids must appear in the aggregated result list.
        Trip trip1 = buildTrip(1L, "user1");
        Trip trip2 = buildTrip(2L, "user1");

        // Trip 1: pin id=10 (null lat → skipped), pin id=11 (usable) → 1 usable → 0 km
        Pin skip1 = buildPin(10L, null,    2.3522, LocalDate.of(2024, 1, 1)); // null lat
        Pin pin11 = buildPin(11L, 48.8566, 2.3522, LocalDate.of(2024, 1, 2));

        // Trip 2: pin id=20 (usable), pin id=21 (null lon → skipped) → 1 usable → 0 km
        Pin pin20 = buildPin(20L, 52.5200, 13.4050, LocalDate.of(2024, 2, 1));
        Pin skip2 = buildPin(21L, 48.8566, null,    LocalDate.of(2024, 2, 2)); // null lon

        when(tripRepository.findByOwnerId("user1")).thenReturn(List.of(trip1, trip2));
        when(pinRepository.findByTripId(1L)).thenReturn(List.of(skip1, pin11));
        when(pinRepository.findByTripId(2L)).thenReturn(List.of(pin20, skip2));

        // Act
        DistanceResult result = mapAPIClient.getTotalDistance("user1", "km");

        // Assert — 0 total distance; both skipped ids present in aggregated list
        assertEquals(0.0, result.getDistanceKm(), 0.0);
        List<Long> skipped = result.getSkippedPinIds();
        assertEquals(2, skipped.size());
        assertTrue(skipped.contains(10L));
        assertTrue(skipped.contains(21L));
    }
}
