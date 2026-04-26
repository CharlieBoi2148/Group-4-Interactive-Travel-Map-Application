package travelmap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import travelmap.map.DistanceCalculator;

/**
 * DistanceCalculatorTest — pure-math input partitioning for FR8.
 *
 * Partitions:
 *   Valid inputs:   same point (zero), short distance, long distance,
 *                   antipodal points, equator crossing, hemisphere crossing,
 *                   prime-meridian crossing.
 *   Boundary cases: north pole (lat=90), south pole (lat=-90),
 *                   antimeridian (lon=180 and lon=-180).
 *   Symmetry:       haversineKm(A,B) == haversineKm(B,A).
 *   Invalid inputs: null lat, null lon, lat > 90, lat < -90,
 *                   lon > 180, lon < -180.
 *   Conversion:     km to miles correct, zero in zero out,
 *                   negative km rejected, monotonically increasing.
 *
 * No Spring context needed — pure unit tests on a static utility class.
 */
class DistanceCalculatorTest {

    private static final double TOLERANCE_KM = 1.0;         // 1 km for long distances
    private static final double TOLERANCE_KM_TIGHT = 0.001; // 1 m for trivial cases

    // ── Valid inputs ──────────────────────────────────────────────────────────

    @Test
    void haversine_returnsZero_whenPointsAreIdentical_FR8() {
        // Arrange
        double lat = 40.7128, lon = -74.0060; // New York

        // Act
        double km = DistanceCalculator.haversineKm(lat, lon, lat, lon);

        // Assert
        assertEquals(0.0, km, TOLERANCE_KM_TIGHT);
    }

    @Test
    void haversine_returnsExpectedDistance_NewYorkToLosAngeles_FR8() {
        // Arrange — known great-circle distance ≈ 3936 km
        double nyLat = 40.7128, nyLon = -74.0060;
        double laLat = 34.0522, laLon = -118.2437;

        // Act
        double km = DistanceCalculator.haversineKm(nyLat, nyLon, laLat, laLon);

        // Assert
        assertEquals(3936.0, km, 5.0);
    }

    @Test
    void haversine_returnsExpectedDistance_LondonToParis_FR8() {
        // Arrange — known great-circle distance ≈ 344 km
        double londonLat = 51.5074, londonLon = -0.1278;
        double parisLat  = 48.8566, parisLon  =  2.3522;

        // Act
        double km = DistanceCalculator.haversineKm(londonLat, londonLon, parisLat, parisLon);

        // Assert
        assertEquals(344.0, km, TOLERANCE_KM);
    }

    @Test
    void haversine_handlesAntipodalPoints_FR8() {
        // Arrange — two points on exactly opposite sides of the Earth
        // Expected distance = π × R ≈ 20015 km
        double lat1 = 0.0, lon1 =   0.0;
        double lat2 = 0.0, lon2 = 180.0;

        // Act
        double km = DistanceCalculator.haversineKm(lat1, lon1, lat2, lon2);

        // Assert
        assertEquals(Math.PI * DistanceCalculator.EARTH_RADIUS_KM, km, TOLERANCE_KM);
    }

    @Test
    void haversine_handlesEquatorCrossing_FR8() {
        // Arrange — northern to southern hemisphere, same longitude
        // 20 degrees of latitude ≈ 2224 km
        double lat1 =  10.0, lon1 = 0.0;
        double lat2 = -10.0, lon2 = 0.0;

        // Act
        double km = DistanceCalculator.haversineKm(lat1, lon1, lat2, lon2);

        // Assert
        assertEquals(2223.9, km, TOLERANCE_KM);
    }

    @Test
    void haversine_handlesPrimeMeridianCrossing_FR8() {
        // Arrange — eastern to western hemisphere along the equator
        // 10 degrees of longitude on equator ≈ 1112 km
        double lat1 = 0.0, lon1 =  5.0;
        double lat2 = 0.0, lon2 = -5.0;

        // Act
        double km = DistanceCalculator.haversineKm(lat1, lon1, lat2, lon2);

        // Assert
        assertEquals(1112.0, km, TOLERANCE_KM);
    }

    // ── Boundary cases ────────────────────────────────────────────────────────

    @Test
    void haversine_acceptsNorthPoleBoundary_FR8() {
        // lat=90 is the exact upper boundary — should not throw
        // North pole to equator on prime meridian ≈ 10007 km
        double km = DistanceCalculator.haversineKm(90.0, 0.0, 0.0, 0.0);

        assertEquals(10007.0, km, TOLERANCE_KM);
    }

    @Test
    void haversine_acceptsSouthPoleBoundary_FR8() {
        // lat=-90 is the exact lower boundary — should not throw
        // South pole to equator on prime meridian ≈ 10007 km (same as north pole)
        double km = DistanceCalculator.haversineKm(-90.0, 0.0, 0.0, 0.0);

        assertEquals(10007.0, km, TOLERANCE_KM);
    }

    @Test
    void haversine_acceptsAntimeridianBoundary_FR8() {
        // lon=180 and lon=-180 are the same physical line — should not throw
        // and distance between them should be zero
        double km = DistanceCalculator.haversineKm(0.0, 180.0, 0.0, -180.0);

        assertEquals(0.0, km, TOLERANCE_KM);
    }

    // ── Symmetry ──────────────────────────────────────────────────────────────

    @Test
    void haversine_isSymmetric_AtoB_equalsBtoA_FR8() {
        // haversineKm(A, B) must equal haversineKm(B, A) — a coding error in
        // the subtraction order before toRadians() would silently break this
        double londonLat = 51.5074, londonLon = -0.1278;
        double parisLat  = 48.8566, parisLon  =  2.3522;

        double ab = DistanceCalculator.haversineKm(londonLat, londonLon, parisLat, parisLon);
        double ba = DistanceCalculator.haversineKm(parisLat, parisLon, londonLat, londonLon);

        assertEquals(ab, ba, TOLERANCE_KM_TIGHT);
    }

    // ── Invalid inputs ────────────────────────────────────────────────────────

    @Test
    void haversine_rejectsNullLat1_FR8() {
        assertThrows(IllegalArgumentException.class,
                () -> DistanceCalculator.haversineKm(null, 0.0, 0.0, 0.0));
    }

    @Test
    void haversine_rejectsNullLon1_FR8() {
        assertThrows(IllegalArgumentException.class,
                () -> DistanceCalculator.haversineKm(0.0, null, 0.0, 0.0));
    }

    @Test
    void haversine_rejectsNullLat2_FR8() {
        assertThrows(IllegalArgumentException.class,
                () -> DistanceCalculator.haversineKm(0.0, 0.0, null, 0.0));
    }

    @Test
    void haversine_rejectsNullLon2_FR8() {
        assertThrows(IllegalArgumentException.class,
                () -> DistanceCalculator.haversineKm(0.0, 0.0, 0.0, null));
    }

    @Test
    void haversine_rejectsLatitudeAbove90_FR8() {
        assertThrows(IllegalArgumentException.class,
                () -> DistanceCalculator.haversineKm(91.0, 0.0, 0.0, 0.0));
    }

    @Test
    void haversine_rejectsLatitudeBelowMinus90_FR8() {
        assertThrows(IllegalArgumentException.class,
                () -> DistanceCalculator.haversineKm(0.0, 0.0, -90.5, 0.0));
    }

    @Test
    void haversine_rejectsLongitudeAbove180_FR8() {
        assertThrows(IllegalArgumentException.class,
                () -> DistanceCalculator.haversineKm(0.0, 181.0, 0.0, 0.0));
    }

    @Test
    void haversine_rejectsLongitudeBelowMinus180_FR8() {
        assertThrows(IllegalArgumentException.class,
                () -> DistanceCalculator.haversineKm(0.0, 0.0, 0.0, -181.0));
    }

    // ── Unit conversion ───────────────────────────────────────────────────────

    @Test
    void kmToMiles_convertsCorrectly_FR8() {
        // 100 km ≈ 62.137 miles
        double miles = DistanceCalculator.kmToMiles(100.0);

        assertEquals(62.137, miles, 0.01);
    }

    @Test
    void kmToMiles_returnsZero_whenInputIsZero_FR8() {
        assertEquals(0.0, DistanceCalculator.kmToMiles(0.0), TOLERANCE_KM_TIGHT);
    }

    @Test
    void kmToMiles_rejectsNegativeInput_FR8() {
        assertThrows(IllegalArgumentException.class,
                () -> DistanceCalculator.kmToMiles(-1.0));
    }

    @Test
    void kmToMiles_isMonotonicallyIncreasing_FR8() {
        // Larger km in should always yield larger miles out
        double smallMi = DistanceCalculator.kmToMiles(10.0);
        double largeMi = DistanceCalculator.kmToMiles(100.0);

        assertTrue(largeMi > smallMi);
    }
}
