package travelmap.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import travelmap.model.Privacy;
import travelmap.model.Trip;
import travelmap.repository.TripService;

@WebMvcTest(TripController.class)
class TripControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TripService tripService;

    @Test
    void createTrip_valid_returns201AndBody() throws Exception {
        Trip request = new Trip();
        request.setName("Italy 2026");
        request.setDescription("Summer");
        request.setStartDate(LocalDate.of(2026, 6, 1));
        request.setEndDate(LocalDate.of(2026, 6, 15));

        Trip saved =
                new Trip(
                        "Italy 2026",
                        "Summer",
                        LocalDate.of(2026, 6, 1),
                        LocalDate.of(2026, 6, 15),
                        null,
                        Privacy.PRIVATE,
                        "user-1");
        saved.setId(1L);

        when(tripService.createTrip(any(Trip.class))).thenReturn(saved);

        mockMvc.perform(
                        post("/api/trips")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Italy 2026"));
    }

    @Test
    void createTrip_invalidName_returns400() throws Exception {
        when(tripService.createTrip(any(Trip.class)))
                .thenThrow(new IllegalArgumentException("Trip name is required"));

        Trip request = new Trip();
        request.setName("");

        mockMvc.perform(
                        post("/api/trips")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllTrips_returns200AndEmptyArray() throws Exception {
        when(tripService.getAllTrips()).thenReturn(List.of());

        mockMvc.perform(get("/api/trips"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getAllTrips_returns200AndTrips() throws Exception {
        Trip t = new Trip("Road trip", null, null, null, null, Privacy.PUBLIC, null);
        t.setId(5L);
        when(tripService.getAllTrips()).thenReturn(List.of(t));

        mockMvc.perform(get("/api/trips"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(5))
                .andExpect(jsonPath("$[0].name").value("Road trip"));
    }
}
