package travelmap;

import travelmap.controller.PinController;
import travelmap.model.Pin;
import travelmap.model.Privacy;
import travelmap.repository.PinService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * PinControllerTest — Unit tests for PinController (Controller layer).
 *
 * Tests the HTTP layer in isolation — verifies that each endpoint:
 *   - Maps to the correct HTTP method and path
 *   - Returns the correct HTTP status code
 *   - Serializes/deserializes JSON correctly
 *   - Delegates to PinService (not the database)
 *
 * @WebMvcTest(PinController.class) loads only the web layer — no full
 * Spring context, no database, no H2. PinService is mocked with @MockBean
 * so we test only PinController's HTTP wiring, not business logic.
 * Business logic is covered by PinServiceTest.
 *
 * MockMvc simulates HTTP requests without starting a real server.
 * It's the standard Spring testing tool for REST controllers.
 *
 * ObjectMapper converts Java objects to JSON strings for request bodies
 * and back. JavaTimeModule is registered to handle LocalDate serialization.
 *
 * Architecture role:
 *   These tests sit at the Controller layer boundary — they confirm that
 *   HTTP requests from React's pinService.js will be handled correctly.
 *   They complement PinServiceTest which covers the business logic layer.
 *
 * SRS coverage:
 *   FR1  — POST /api/pins → HTTP 201
 *   FR2  — PUT /api/pins/{id} → HTTP 200
 *   FR3  — DELETE /api/pins/{id} → HTTP 204
 *   FR4  — GET /api/pins → HTTP 200
 *   FR11 — PATCH /api/pins/{id}/privacy → HTTP 200
 */
@WebMvcTest(PinController.class)
class PinControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PinService pinService;

    private ObjectMapper objectMapper;

    /**
     * Runs before each test — sets up ObjectMapper with JavaTimeModule
     * so LocalDate fields serialize correctly to/from JSON.
     * Without JavaTimeModule, Jackson cannot handle java.time.LocalDate.
     */
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    // ── Helper — builds a valid Pin for reuse across tests ───────────────────

    private Pin buildPin(Long id, String locationName) {
        Pin pin = new Pin();
        pin.setId(id);
        pin.setLocationName(locationName);
        pin.setCountry("France");
        pin.setRegion("Ile-de-France");
        pin.setVisitDate(LocalDate.of(2024, 6, 1));
        pin.setLatitude(48.8584);
        pin.setLongitude(2.2945);
        pin.setPrivacyLevel(Privacy.PRIVATE);
        return pin;
    }

    // ── FR1: POST /api/pins ───────────────────────────────────────────────────

    @Test
    void createPin_returns201_withSavedPin() throws Exception {
        // Arrange
        Pin input = buildPin(null, "Eiffel Tower");
        Pin saved = buildPin(1L, "Eiffel Tower");
        when(pinService.createPin(any(Pin.class))).thenReturn(saved);

        // Act + Assert
        mockMvc.perform(post("/api/pins")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())               // HTTP 201
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.locationName").value("Eiffel Tower"))
                .andExpect(jsonPath("$.latitude").value(48.8584))
                .andExpect(jsonPath("$.longitude").value(2.2945));
    }

    @Test
    void createPin_returns400_whenServiceThrowsIllegalArgument() throws Exception {
        // Arrange — simulates missing locationName or coordinates
        Pin input = buildPin(null, null);
        when(pinService.createPin(any(Pin.class)))
            .thenThrow(new IllegalArgumentException("Location name is required"));

        // Act + Assert
        mockMvc.perform(post("/api/pins")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest()); // HTTP 400
    }

    // ── FR4: GET /api/pins ────────────────────────────────────────────────────

    @Test
    void getAllPins_returns200_withPinList() throws Exception {
        // Arrange
        Pin pin1 = buildPin(1L, "Eiffel Tower");
        Pin pin2 = buildPin(2L, "Colosseum");
        when(pinService.getAllPins()).thenReturn(List.of(pin1, pin2));

        // Act + Assert
        mockMvc.perform(get("/api/pins"))
                .andExpect(status().isOk())                    // HTTP 200
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].locationName").value("Eiffel Tower"))
                .andExpect(jsonPath("$[1].locationName").value("Colosseum"));
    }

    @Test
    void getAllPins_returns200_withEmptyList_whenNoPinsExist() throws Exception {
        // Arrange
        when(pinService.getAllPins()).thenReturn(List.of());

        // Act + Assert — empty array not null, React can safely call .map()
        mockMvc.perform(get("/api/pins"))
                .andExpect(status().isOk())                    // HTTP 200
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ── FR2: PUT /api/pins/{id} ───────────────────────────────────────────────

    @Test
    void updatePin_returns200_withUpdatedPin() throws Exception {
        // Arrange
        Pin updates = buildPin(null, "Updated Tower");
        Pin updated = buildPin(1L, "Updated Tower");
        when(pinService.updatePin(eq(1L), any(Pin.class))).thenReturn(updated);

        // Act + Assert
        mockMvc.perform(put("/api/pins/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk())                    // HTTP 200
                .andExpect(jsonPath("$.locationName").value("Updated Tower"));
    }

    @Test
    void updatePin_returns404_whenPinNotFound() throws Exception {
        // Arrange
        when(pinService.updatePin(eq(99L), any(Pin.class)))
            .thenThrow(new IllegalArgumentException("Pin not found: 99"));

        // Act + Assert
        mockMvc.perform(put("/api/pins/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new Pin())))
                .andExpect(status().isNotFound());             // HTTP 404
    }

    // ── FR3: DELETE /api/pins/{id} ────────────────────────────────────────────

    @Test
    void deletePin_returns204_onSuccess() throws Exception {
        // Arrange — doNothing is the default for void methods but explicit here
        doNothing().when(pinService).deletePin(1L);

        // Act + Assert
        mockMvc.perform(delete("/api/pins/1"))
                .andExpect(status().isNoContent());            // HTTP 204
    }

    @Test
    void deletePin_returns404_whenPinNotFound() throws Exception {
        // Arrange
        doThrow(new IllegalArgumentException("Pin not found: 99"))
            .when(pinService).deletePin(99L);

        // Act + Assert
        mockMvc.perform(delete("/api/pins/99"))
                .andExpect(status().isNotFound());             // HTTP 404
    }

    // ── FR11: PATCH /api/pins/{id}/privacy ───────────────────────────────────

    @Test
    void setPinPrivacy_returns200_withUpdatedPin() throws Exception {
        // Arrange
        Pin updated = buildPin(1L, "Eiffel Tower");
        updated.setPrivacyLevel(Privacy.PUBLIC);
        when(pinService.setPinPrivacy(eq(1L), eq(Privacy.PUBLIC))).thenReturn(updated);

        // Act + Assert
        mockMvc.perform(patch("/api/pins/1/privacy")
                .contentType(MediaType.APPLICATION_JSON)
                .content("\"PUBLIC\""))                        // JSON string enum value
                .andExpect(status().isOk())                    // HTTP 200
                .andExpect(jsonPath("$.privacyLevel").value("PUBLIC"));
    }

    @Test
    void setPinPrivacy_returns404_whenPinNotFound() throws Exception {
        // Arrange
        when(pinService.setPinPrivacy(eq(99L), any(Privacy.class)))
            .thenThrow(new IllegalArgumentException("Pin not found: 99"));

        // Act + Assert
        mockMvc.perform(patch("/api/pins/99/privacy")
                .contentType(MediaType.APPLICATION_JSON)
                .content("\"PUBLIC\""))
                .andExpect(status().isNotFound());             // HTTP 404
    }
}