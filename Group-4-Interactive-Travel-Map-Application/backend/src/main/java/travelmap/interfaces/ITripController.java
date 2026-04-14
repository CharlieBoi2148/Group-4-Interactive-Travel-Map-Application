package travelmap.interfaces;

import java.util.List;

import org.springframework.http.ResponseEntity;

import travelmap.model.Trip;

/**
 * Trip REST contract.
 *
 * <p>Maps to {@code POST /api/trips} and {@code GET /api/trips} on
 * {@link travelmap.controller.TripController} — FR5 (create/organize trip)
 * and FR4 (view trips).
 */
public interface ITripController {

    /**
     * FR5 — Create and persist a new trip ({@code POST /api/trips}).
     *
     * @param trip request body
     * @return HTTP 201 with saved trip, or 400 if validation fails
     */
    ResponseEntity<Trip> createTrip(Trip trip);

    /**
     * FR4 — List all persisted trips ({@code GET /api/trips}).
     *
     * @return HTTP 200 with a JSON array of trips
     */
    ResponseEntity<List<Trip>> getAllTrips();
}
