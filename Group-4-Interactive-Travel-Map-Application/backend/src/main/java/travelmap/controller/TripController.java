package travelmap.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import travelmap.interfaces.ITripController;
import travelmap.model.Trip;
import travelmap.repository.TripService;

/**
 * REST controller for trips. Delegates persistence and rules to {@link TripService}.
 */
@RestController
@RequestMapping("/api/trips")
@CrossOrigin(origins = "http://localhost:3000")
public class TripController implements ITripController {

    private final TripService tripService;

    /**
     * @param tripService trip business layer
     */
    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PostMapping
    public ResponseEntity<Trip> createTrip(@RequestBody Trip trip) {
        try {
            Trip saved = tripService.createTrip(trip);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @GetMapping
    public ResponseEntity<List<Trip>> getAllTrips() {
        return ResponseEntity.ok(tripService.getAllTrips());
    }
}
