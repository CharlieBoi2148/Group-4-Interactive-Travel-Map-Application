package travelmap.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import travelmap.interfaces.ISearchController;
import travelmap.model.Pin;
import travelmap.repository.PinService;

@RestController
@RequestMapping("/api/search")
@CrossOrigin(origins = "http://localhost:3000")
public class SearchController implements ISearchController {

    private final PinService pinService;

    @Autowired
    public SearchController(PinService pinService) {
        this.pinService = pinService;
    }

    /**
     * FR9 — Search pins by keyword.
     *
     * @param keyword optional keyword for location-name matching
     * @return HTTP 200 with matching pins
     */
    @GetMapping
    public ResponseEntity<List<Pin>> searchPins(@RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(pinService.searchPins(keyword));
    }

    public void handleFilterSearch(Object criteria) {}
    public void handleViewStatistics() {}
}
