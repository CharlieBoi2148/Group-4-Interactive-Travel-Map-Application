package travelmap.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import travelmap.account.AccountFacade;
import travelmap.model.Pin;
import travelmap.model.Trip;
import travelmap.model.User;
import travelmap.repository.PinService;
import travelmap.repository.TripService;

@WebMvcTest(SearchController.class)
class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PinService pinService;

    @MockBean
    private TripService tripService;

    @MockBean
    private AccountFacade accountFacade;

    private User signedInUser() {
        User u = new User();
        u.setUsername("user-1");
        return u;
    }

    private Trip ownerTrip(long id) {
        Trip t = new Trip();
        t.setId(id);
        t.setOwnerId("user-1");
        t.setName("Owner trip");
        return t;
    }

    @Test
    void searchPins_noKeyword_returns200AndArray() throws Exception {
        when(accountFacade.getCurrentUser()).thenReturn(signedInUser());
        when(tripService.getTripsForOwner("user-1")).thenReturn(List.of(ownerTrip(1L)));
        when(pinService.searchPinsByTripIds(null, Set.of(1L))).thenReturn(List.of());

        mockMvc.perform(get("/api/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void searchPins_keyword_returns200AndPins() throws Exception {
        Pin p = new Pin();
        p.setId(1L);
        p.setLocationName("Eiffel Tower");
        p.setVisitDate(LocalDate.of(2024, 6, 1));
        p.setLatitude(48.8584);
        p.setLongitude(2.2945);

        when(accountFacade.getCurrentUser()).thenReturn(signedInUser());
        when(tripService.getTripsForOwner("user-1")).thenReturn(List.of(ownerTrip(1L)));
        when(pinService.searchPinsByTripIds("eiffel", Set.of(1L))).thenReturn(List.of(p));

        mockMvc.perform(get("/api/search").param("keyword", "eiffel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].locationName").value("Eiffel Tower"));
    }

    @Test
    void searchPins_blankKeywordParam_returns200AndArray() throws Exception {
        when(accountFacade.getCurrentUser()).thenReturn(signedInUser());
        when(tripService.getTripsForOwner("user-1")).thenReturn(List.of(ownerTrip(1L)));
        when(pinService.searchPinsByTripIds("   ", Set.of(1L))).thenReturn(List.of());

        mockMvc.perform(get("/api/search").param("keyword", "   "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void searchPins_unauthenticated_returns401() throws Exception {
        when(accountFacade.getCurrentUser()).thenReturn(null);

        mockMvc.perform(get("/api/search"))
                .andExpect(status().isUnauthorized());
    }
}

