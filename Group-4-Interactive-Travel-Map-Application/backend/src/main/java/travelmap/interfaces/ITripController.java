package travelmap.interfaces;

import java.util.List;

import org.springframework.http.ResponseEntity;

import travelmap.model.Trip;

/**
 * Trip REST contract (FR5 — organize trip; FR4 — view trips).
 */
public interface ITripController {

    /**
     * Create and persist a new trip.
     *
     * @param trip request body
     * @return HTTP 201 with saved trip, or 400 if validation fails
     */
    ResponseEntity<Trip> createTrip(Trip trip);

    /**
     * List all persisted trips.
     *
     * @return 200 with trip list
     */
    ResponseEntity<List<Trip>> getAllTrips();
}
