package travelmap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import travelmap.controller.MapController;
import travelmap.interfaces.MapService;
import travelmap.map.DistanceResult;

/**
 * MapControllerTest — MVC slice tests for the FR8 distance API.
 *
 * Uses @WebMvcTest to load only MapController and the MVC layer (Jackson,
 * request mapping, parameter binding). MapService is mocked via @MockBean —
 * no database, no Haversine math, no Spring Security context needed.
 *
 * These tests verify that MapController:
 *   - Routes requests to the correct endpoint
 *   - Binds @RequestParam and @PathVariable correctly
 *   - Returns the correct HTTP status codes (200, 400, 404)
 *   - Serializes DistanceResult to JSON correctly
 *   - Delegates to MapService and does nothing else
 *
 * Input partitioning:
 *   getDistanceBetween — valid coords (km), valid coords (mi, FR14),
 *                        missing required param (400), IAE from service (400)
 *   getTripDistance    — valid tripId (200), trip not found IAE (404),
 *                        skippedPinIds in response
 *   getTotalDistance   — valid ownerId (200), blank ownerId IAE (400),
 *                        missing ownerId param (400)
 *
 * SRS coverage:
 *   FR8  — all three endpoints
 *   FR14 — unit param propagated and returned in response
 */
@WebMvcTest(MapController.class)
class MapControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MapService mapService;

    // ── getDistanceBetween ────────────────────────────────────────────────────

    @Test
    void getDistanceBetween_validCoords_returns200WithDistanceResult_FR8()
            throws Exception {
        // Arrange
        DistanceResult result = new DistanceResult(344.0, "km", null);
        when(mapService.getDistanceBetween(any(), any(), any(), any(), anyString()))
                .thenReturn(result);

        // Act + Assert
        mockMvc.perform(get("/api/map/distance")
                .param("lat1", "51.5074").param("lon1", "-0.1278")
                .param("lat2", "48.8566").param("lon2", "2.3522")
                .param("unit", "km"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.distanceKm").value(344.0))
            .andExpect(jsonPath("$.unit").value("km"))
            .andExpect(jsonPath("$.skippedPinIds").isArray());
    }

    @Test
    void getDistanceBetween_unitMi_returnsUnitMiInResponse_FR8_FR14()
            throws Exception {
        // Arrange — unit=mi must be propagated through and reflected in response
        DistanceResult result = new DistanceResult(344.0, "mi", null);
        when(mapService.getDistanceBetween(any(), any(), any(), any(), anyString()))
                .thenReturn(result);

        // Act + Assert
        mockMvc.perform(get("/api/map/distance")
                .param("lat1", "51.5074").param("lon1", "-0.1278")
                .param("lat2", "48.8566").param("lon2", "2.3522")
                .param("unit", "mi"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.unit").value("mi"));
    }

    @Test
    void getDistanceBetween_missingRequiredParam_returns400_FR8()
            throws Exception {
        // Spring returns 400 automatically when a required @RequestParam is absent
        mockMvc.perform(get("/api/map/distance")
                .param("lat1", "51.5074").param("lon1", "-0.1278")
                .param("lat2", "48.8566"))
            // lon2 missing — Spring MVC rejects before reaching controller
            .andExpect(status().isBadRequest());
    }

    @Test
    void getDistanceBetween_serviceThrowsIAE_returns400_FR8()
            throws Exception {
        // Arrange — service rejects invalid coordinates
        when(mapService.getDistanceBetween(any(), any(), any(), any(), anyString()))
                .thenThrow(new IllegalArgumentException("Latitude out of range"));

        // Act + Assert
        mockMvc.perform(get("/api/map/distance")
                .param("lat1", "91.0").param("lon1", "0.0")
                .param("lat2", "0.0").param("lon2", "0.0"))
            .andExpect(status().isBadRequest());
    }

    // ── getTripDistance ───────────────────────────────────────────────────────

    @Test
    void getTripDistance_validTripId_returns200WithDistanceResult_FR8()
            throws Exception {
        // Arrange
        DistanceResult result = new DistanceResult(500.0, "km", null);
        when(mapService.getTripDistance(anyLong(), anyString()))
                .thenReturn(result);

        // Act + Assert
        mockMvc.perform(get("/api/map/distance/trip/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.distanceKm").value(500.0))
            .andExpect(jsonPath("$.skippedPinIds").isArray());
    }

    @Test
    void getTripDistance_tripNotFound_returns404_FR8()
            throws Exception {
        // Arrange — service throws IAE when trip does not exist
        when(mapService.getTripDistance(anyLong(), anyString()))
                .thenThrow(new IllegalArgumentException("Trip not found: 999"));

        // Act + Assert
        mockMvc.perform(get("/api/map/distance/trip/999"))
            .andExpect(status().isNotFound());
    }

    @Test
    void getTripDistance_skippedPinIdsInResponse_FR8()
            throws Exception {
        // Arrange — two pins were skipped due to null coordinates
        DistanceResult result = new DistanceResult(200.0, "km", List.of(3L, 7L));
        when(mapService.getTripDistance(anyLong(), anyString()))
                .thenReturn(result);

        // Act + Assert
        mockMvc.perform(get("/api/map/distance/trip/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.skippedPinIds.length()").value(2))
            .andExpect(jsonPath("$.skippedPinIds[0]").value(3))
            .andExpect(jsonPath("$.skippedPinIds[1]").value(7));
    }

    @Test
    void getTripDistance_defaultsToKm_whenUnitAbsent_FR8()
            throws Exception {
        // Arrange — no unit param, default "km" must be used
        DistanceResult result = new DistanceResult(300.0, "km", null);
        when(mapService.getTripDistance(anyLong(), anyString()))
                .thenReturn(result);

        // Act + Assert
        mockMvc.perform(get("/api/map/distance/trip/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.unit").value("km"));
    }

    // ── getTotalDistance ──────────────────────────────────────────────────────

    @Test
    void getTotalDistance_validOwnerId_returns200WithDistanceResult_FR8()
            throws Exception {
        // Arrange
        DistanceResult result = new DistanceResult(1200.0, "km",
                Collections.emptyList());
        when(mapService.getTotalDistance(anyString(), anyString()))
                .thenReturn(result);

        // Act + Assert
        mockMvc.perform(get("/api/map/distance/total")
                .param("ownerId", "user-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.distanceKm").value(1200.0));
    }

    @Test
    void getTotalDistance_missingOwnerIdParam_returns400_FR8()
            throws Exception {
        // Spring returns 400 automatically when required @RequestParam is absent
        mockMvc.perform(get("/api/map/distance/total"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getTotalDistance_serviceThrowsIAE_blankOwnerId_returns400_FR8()
            throws Exception {
        // Arrange — service rejects blank ownerId
        when(mapService.getTotalDistance(anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("ownerId must not be blank"));

        // Act + Assert
        mockMvc.perform(get("/api/map/distance/total")
                .param("ownerId", "   "))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getTotalDistance_zeroDistance_whenNoTrips_FR8()
            throws Exception {
        // Arrange — user exists but has no trips
        DistanceResult result = new DistanceResult(0.0, "km",
                Collections.emptyList());
        when(mapService.getTotalDistance(anyString(), anyString()))
                .thenReturn(result);

        // Act + Assert
        mockMvc.perform(get("/api/map/distance/total")
                .param("ownerId", "user-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.distanceKm").value(0.0))
            .andExpect(jsonPath("$.skippedPinIds").isEmpty());
    }
}
