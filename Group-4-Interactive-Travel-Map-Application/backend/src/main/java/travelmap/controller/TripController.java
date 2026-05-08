package travelmap.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import travelmap.account.AccountFacade;
import travelmap.interfaces.ITripController;
import travelmap.model.Privacy;
import travelmap.model.Trip;
import travelmap.model.User;
import travelmap.repository.TripService;

/**
 * TripController — Controller layer (MVC).
 *
 * <p>Handles HTTP for trips from the React app (or any JSON client). Delegates
 * to {@link TripService}; no persistence or business rules here.
 *
 * <p><b>SRS:</b>
 * <ul>
 *   <li><b>FR5 — Organize Trip:</b> user creates a trip with name, description,
 *       dates; pins are assigned in later increments. {@code POST /api/trips}
 *       creates the trip record.</li>
 *   <li><b>FR4 — View Pins and Trips:</b> dashboard/map load trips for the user.
 *       {@code GET /api/trips} returns all stored trips as JSON (empty array if none).</li>
 * </ul>
 *
 * <p><b>CORS:</b> {@code @CrossOrigin} allows the browser to call this API from
 * React on {@code http://localhost:3000}. Replace with a {@code WebMvcConfigurer}
 * CORS bean before production (see project context).
 *
 * <p><b>TODO (Wilson — FR4, NFR4):</b> After auth merges, scope {@link #getAllTrips()}
 * (and create) to the authenticated owner instead of returning every trip.
 */
@RestController
@RequestMapping("/api/trips")
@CrossOrigin(origins = "http://localhost:3000")
public class TripController implements ITripController {

    private final TripService tripService;
    private final AccountFacade accountFacade;

    /**
     * @param tripService trip business layer (constructor injection)
     */
    @Autowired
    public TripController(TripService tripService, AccountFacade accountFacade) {
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
     * FR5 — Create a new trip.
     *
     * <p>Request body is a {@link Trip} JSON object (id omitted or null for create).
     * {@link TripService#createTrip(Trip)} requires a non-blank {@code name} and
     * defaults {@code privacyLevel} to {@code PRIVATE} when null.
     *
     * @param trip deserialized trip from the request body
     * @return HTTP 201 with the persisted trip, or HTTP 400 if validation fails
     */
    @Override
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Trip> createTrip(@RequestBody Trip trip) {
        try {
            String ownerId = resolveOwnerId();
            if (ownerId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            trip.setOwnerId(ownerId);
            Trip saved = tripService.createTrip(trip);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * FR4 — List all trips as JSON.
     *
     * <p>Returns an empty array when no trips exist so clients can safely iterate.
     * Target load within 2 seconds per SRS (dev H2; production may use PostgreSQL).
     *
     * @return HTTP 200 with a JSON array of trips
     */
    @Override
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Trip>> getAllTrips() {
        String ownerId = resolveOwnerId();
        if (ownerId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(tripService.getTripsForOwner(ownerId));
    }

    /**
     * FR5 — Edit an existing trip.
     *
     * @param id trip id from path
     * @param trip updated trip payload
     * @return HTTP 200 with updated trip, 400 for invalid payload, or 404 when missing
     */
    @Override
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Trip> updateTrip(@PathVariable Long id, @RequestBody Trip trip) {
        try {
            String ownerId = resolveOwnerId();
            if (ownerId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            return tripService.updateTripForOwner(id, trip, ownerId)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * FR11 — Update trip privacy.
     *
     * <p>Uses PATCH because only one field is being changed.
     *
     * @param id trip id from path
     * @param privacyLevel new privacy enum value from request body
     * @return HTTP 200 with updated trip, or 404 if trip does not exist
     */
    @Override
    @PatchMapping(value = "/{id}/privacy", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Trip> setTripPrivacy(@PathVariable Long id, @RequestBody Privacy privacyLevel) {
        String ownerId = resolveOwnerId();
        if (ownerId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return tripService.setTripPrivacyForOwner(id, privacyLevel, ownerId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
