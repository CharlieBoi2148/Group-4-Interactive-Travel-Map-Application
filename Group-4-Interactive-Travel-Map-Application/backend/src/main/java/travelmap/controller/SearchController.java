package travelmap.controller;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import travelmap.account.AccountFacade;
import travelmap.interfaces.ISearchController;
import travelmap.model.Pin;
import travelmap.model.Trip;
import travelmap.model.User;
import travelmap.repository.PinService;
import travelmap.repository.TripService;

@RestController
@RequestMapping("/api/search")
@CrossOrigin(origins = "http://localhost:3000")
public class SearchController implements ISearchController {

    private final PinService pinService;
    private final TripService tripService;
    private final AccountFacade accountFacade;

    @Autowired
    public SearchController(PinService pinService, TripService tripService, AccountFacade accountFacade) {
        this.pinService = pinService;
        this.tripService = tripService;
        this.accountFacade = accountFacade;
    }

    private String resolveOwnerId() {
        User user = accountFacade.getCurrentUser();
        if (user == null) return null;
        String userId = user.getUserId();
        if (userId != null && !userId.isBlank()) return userId;
        String username = user.getUsername();
        return (username != null && !username.isBlank()) ? username : null;
    }

    /**
     * FR9 — Search / filter pins by optional keyword and optional trip.
     *
     * @param keyword optional keyword for location-name matching
     * @param tripId when set, only pins assigned to this trip are returned (must belong to the user)
     * @return HTTP 200 with matching pins; HTTP 404 if {@code tripId} is not found or not owned
     */
    @GetMapping
    public ResponseEntity<List<Pin>> searchPins(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long tripId) {
        String ownerId = resolveOwnerId();
        if (ownerId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        if (tripId != null) {
            Optional<Trip> trip = tripService.getTripById(tripId);
            if (trip.isEmpty() || trip.get().getOwnerId() == null || !ownerId.equals(trip.get().getOwnerId())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.ok(pinService.searchPinsForSingleTrip(tripId, keyword, ownerId));
        }

        Set<Long> ownerTripIds = new LinkedHashSet<>();
        tripService.getTripsForOwner(ownerId).forEach(trip -> {
            if (trip.getId() != null) ownerTripIds.add(trip.getId());
        });
        return ResponseEntity.ok(pinService.searchPinsByTripIds(keyword, ownerTripIds, ownerId));
    }

    public void handleFilterSearch(Object criteria) {}
    public void handleViewStatistics() {}
}
