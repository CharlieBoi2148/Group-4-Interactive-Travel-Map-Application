package travelmap.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import travelmap.model.Pin;
import travelmap.repository.PinService;

@WebMvcTest(SearchController.class)
class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PinService pinService;

    @Test
    void searchPins_noKeyword_returns200AndArray() throws Exception {
        when(pinService.searchPins(null)).thenReturn(List.of());

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

        when(pinService.searchPins("eiffel")).thenReturn(List.of(p));

        mockMvc.perform(get("/api/search").param("keyword", "eiffel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].locationName").value("Eiffel Tower"));
    }
}

