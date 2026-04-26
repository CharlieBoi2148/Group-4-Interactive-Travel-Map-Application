package travelmap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import travelmap.map.DistanceCalculator;
import travelmap.map.DistanceResult;

/**
 * DistanceResultTest — input partitioning for the DistanceResult constructor (FR8, FR14).
 *
 * Partitions:
 *   unit parameter:    "km" explicit, "mi" explicit, case-insensitive "MI",
 *                      null (defaults to km), unrecognised string (defaults to km).
 *   distanceKm:        zero, positive value, negative (rejected — constructor throws).
 *   skippedPinIds:     null (returns empty list), empty list, non-empty list,
 *                      defensive copy (caller mutation ignored),
 *                      returned list is unmodifiable.
 *   computed fields:   preferred mirrors distanceKm when unit=km,
 *                      preferred mirrors distanceMi when unit=mi.
 *
 * No Spring context — pure constructor/getter unit tests.
 */
class DistanceResultTest {

    private static final double TOLERANCE = 0.001;

    // ── unit parameter partitions ─────────────────────────────────────────────

    @Test
    void unit_km_setsUnitFieldAndPrefersDistanceKm_FR8() {
        // Arrange + Act
        DistanceResult result = new DistanceResult(100.0, "km", null);

        // Assert
        assertEquals("km", result.getUnit());
        assertEquals(100.0, result.getPreferred(), TOLERANCE);
        assertEquals(100.0, result.getDistanceKm(), TOLERANCE);
    }

    @Test
    void unit_mi_setsUnitFieldAndPrefersDistanceMi_FR8_FR14() {
        // Arrange + Act
        DistanceResult result = new DistanceResult(100.0, "mi", null);

        // Assert — preferred must equal the computed mi value, not km
        double expectedMi = DistanceCalculator.kmToMiles(100.0);
        assertEquals("mi", result.getUnit());
        assertEquals(expectedMi, result.getPreferred(), TOLERANCE);
        assertEquals(expectedMi, result.getDistanceMi(), TOLERANCE);
    }

    @Test
    void unit_caseInsensitiveMI_treatedAsMi_FR8_FR14() {
        // "MI", "Mi", "mI" must all normalise to "mi"
        DistanceResult upper  = new DistanceResult(50.0, "MI", null);
        DistanceResult mixed  = new DistanceResult(50.0, "Mi", null);

        assertEquals("mi", upper.getUnit());
        assertEquals("mi", mixed.getUnit());
    }

    @Test
    void unit_null_defaultsToKm_FR8() {
        // Arrange + Act
        DistanceResult result = new DistanceResult(100.0, null, null);

        // Assert — null unit must fall through to "km"
        assertEquals("km", result.getUnit());
        assertEquals(100.0, result.getPreferred(), TOLERANCE);
    }

    @Test
    void unit_unrecognised_defaultsToKm_FR8() {
        // Arrange + Act — "furlongs" is not "km" or "mi"
        DistanceResult result = new DistanceResult(100.0, "furlongs", null);

        // Assert — unrecognised unit falls through to "km"
        assertEquals("km", result.getUnit());
        assertEquals(100.0, result.getPreferred(), TOLERANCE);
    }

    // ── distanceKm partitions ─────────────────────────────────────────────────

    @Test
    void distanceKm_zero_producesBothFieldsAsZero_FR8() {
        // Arrange + Act
        DistanceResult result = new DistanceResult(0.0, "km", null);

        // Assert
        assertEquals(0.0, result.getDistanceKm(), TOLERANCE);
        assertEquals(0.0, result.getDistanceMi(), TOLERANCE);
        assertEquals(0.0, result.getPreferred(), TOLERANCE);
    }

    @Test
    void distanceKm_positive_computesMiCorrectly_FR8() {
        // Arrange + Act — 100 km ≈ 62.137 mi
        DistanceResult result = new DistanceResult(100.0, "km", null);

        // Assert — mi must match DistanceCalculator's own conversion
        assertEquals(DistanceCalculator.kmToMiles(100.0), result.getDistanceMi(), TOLERANCE);
    }

    @Test
    void distanceKm_negative_throwsIllegalArgumentException_FR8() {
        // DistanceCalculator.kmToMiles rejects negative values —
        // the constructor must propagate this rather than silently storing -km
        assertThrows(IllegalArgumentException.class,
                () -> new DistanceResult(-1.0, "km", null));
    }

    // ── skippedPinIds partitions ──────────────────────────────────────────────

    @Test
    void skippedPinIds_null_returnsEmptyList_FR8() {
        // Arrange + Act
        DistanceResult result = new DistanceResult(10.0, "km", null);

        // Assert — must never return null; empty list is safe for React .map()
        List<Long> ids = result.getSkippedPinIds();
        assertTrue(ids != null && ids.isEmpty());
    }

    @Test
    void skippedPinIds_emptyList_returnsEmptyList_FR8() {
        // Arrange + Act
        DistanceResult result = new DistanceResult(10.0, "km", List.of());

        // Assert
        assertTrue(result.getSkippedPinIds().isEmpty());
    }

    @Test
    void skippedPinIds_nonEmpty_returnsCorrectIds_FR8() {
        // Arrange
        List<Long> skipped = List.of(3L, 7L, 42L);

        // Act
        DistanceResult result = new DistanceResult(10.0, "km", skipped);

        // Assert
        assertEquals(List.of(3L, 7L, 42L), result.getSkippedPinIds());
    }

    @Test
    void skippedPinIds_defensiveCopy_callerMutationIgnored_FR8() {
        // Arrange — mutable list passed to constructor
        List<Long> original = new ArrayList<>(List.of(1L, 2L));
        DistanceResult result = new DistanceResult(10.0, "km", original);

        // Act — caller mutates the original list after construction
        original.add(99L);

        // Assert — internal list must be unaffected
        assertEquals(2, result.getSkippedPinIds().size());
    }

    @Test
    void skippedPinIds_returnedListIsUnmodifiable_FR8() {
        // Arrange
        DistanceResult result = new DistanceResult(10.0, "km", List.of(1L));

        // Act + Assert — adding to the returned list must throw
        assertThrows(UnsupportedOperationException.class,
                () -> result.getSkippedPinIds().add(99L));
    }

    @Test
    void distanceKm_stillPopulated_whenUnitIsMi_FR8() {
        DistanceResult result = new DistanceResult(100.0, "mi", null);

        assertEquals(100.0, result.getDistanceKm(), TOLERANCE);
    }
}
