package travelmap.controller;

import travelmap.account.AccountFacade;
import travelmap.interfaces.IPinController;
import travelmap.model.Pin;
import travelmap.model.Privacy;
import travelmap.model.User;
import travelmap.repository.PinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * PinController — Controller layer (MVC).
 *
 * Handles all incoming HTTP requests related to pins from the React frontend.
 * This is the entry point for all pin operations on the backend — it receives
 * JSON requests, delegates to PinService for business logic, and returns
 * JSON responses. No business logic lives here.
 *
 * Architecture role:
 *   - Sits directly above PinService in the backend stack.
 *   - React's pinService.js sends fetch() calls to the endpoints defined here.
 *   - PinController delegates immediately to PinService — no direct DB access.
 *   - Implements IPinController so AppController can wire it via the interface,
 *     satisfying DIP and ISP from the SOLID principles documented in the SRS.
 *   - @RestController combines @Controller and @ResponseBody — all return values
 *     are automatically serialized to JSON by Spring's Jackson library.
 *   - @RequestMapping("/api/pins") prefixes all endpoint paths in this class.
 *
 * Return types:
 *   All methods return ResponseEntity<T> instead of T directly. This gives
 *   explicit control over the HTTP status code in both success and error cases.
 *   Without ResponseEntity, Spring defaults to HTTP 200 for all successful
 *   responses and HTTP 500 for any unhandled exception — which is misleading
 *   to the React frontend and makes debugging harder.
 *
 * Error handling:
 *   Each method catches IllegalArgumentException from PinService and returns
 *   the appropriate HTTP error code (400 or 404) with the exception message.
 *   This is a simple per-method approach appropriate for the current scope.
 *
 *   TODO (robustness — shared task):
 *   As the project grows, consider replacing per-method try/catch with a
 *   global @ControllerAdvice exception handler class. This centralizes error
 *   handling across all controllers and avoids repetition. Appropriate to
 *   implement during the Week 3 cleanup sprint before final submission.
 *
 * CORS:
 *   @CrossOrigin allows requests from React running on localhost:3000.
 *   Without this the browser blocks fetch() calls from port 3000 to port 8080
 *   due to the Same-Origin Policy. Development-only configuration.
 *
 *   TODO (deployment):
 *   Replace @CrossOrigin("http://localhost:3000") with a proper CORS
 *   configuration class before deploying to production. The allowed origin
 *   should be the production React URL, not localhost.
 *
 * SRS coverage:
 *   FR1  — createPin()     POST   /api/pins              → HTTP 201
 *   FR2  — updatePin()     PUT    /api/pins/{id}          → HTTP 200
 *   FR3  — deletePin()     DELETE /api/pins/{id}          → HTTP 204
 *   FR4  — getAllPins()    GET    /api/pins                → HTTP 200
 *   FR11 — setPinPrivacy() PATCH  /api/pins/{id}/privacy  → HTTP 200
 */
@RestController
@RequestMapping("/api/pins")
@CrossOrigin(origins = "http://localhost:3000")
public class PinController implements IPinController {

    private final PinService pinService;
    private final AccountFacade accountFacade;

    /**
     * Constructor injection — PinService and AccountFacade provided by Spring on startup.
     *
     * @param pinService     the service handling all pin business logic
     * @param accountFacade  provides the authenticated user via getCurrentUser()
     */
    @Autowired
    public PinController(PinService pinService, AccountFacade accountFacade) {
        this.pinService = pinService;
        this.accountFacade = accountFacade;
    }

    /**
     * FR1 — Create a new travel pin.
     *
     * Called by React's pinService.js when user clicks Save in PinForm.jsx.
     * Receives pin data as JSON, delegates to PinService for validation and
     * persistence, and returns the saved Pin as HTTP 201 Created.
     *
     * HTTP 201 is used instead of 200 because a new resource was created.
     * React uses the returned id to identify this pin for future edit/delete.
     *
     * Returns HTTP 400 Bad Request if PinService throws IllegalArgumentException
     * — this happens when locationName or coordinates are missing per FR1.
     *
     * Example request body from pinService.js:
     * {
     *   "locationName": "Eiffel Tower",
     *   "country": "France",
     *   "region": "Ile-de-France",
     *   "visitDate": "2024-06-01",
     *   "notes": "Amazing view",
     *   "latitude": 48.8584,
     *   "longitude": 2.2945,
     *   "privacyLevel": "PRIVATE"
     * }
     *
     * @param pin Pin object deserialized from JSON request body by Jackson
     * @return HTTP 201 with saved Pin, or HTTP 400 if validation fails
     */
    @Override
    @PostMapping
    public ResponseEntity<Pin> createPin(@RequestBody Pin pin) {
        User currentUser = accountFacade.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            Pin saved = pinService.createPin(pin, currentUser.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * FR4 — Retrieve all pins belonging to the authenticated user.
     *
     * Called by MapView.jsx on component mount via useEffect — this is what
     * makes pins reload on page refresh. Returns an empty array if no pins
     * exist so React can safely call .map() on the result.
     *
     * Pins must load within 2 seconds per SRS FR4. H2 is fast enough for
     * development. PostgreSQL with proper indexing handles production load.
     *
     * Returns HTTP 401 if no user is logged in — AccountFacade.getCurrentUser()
     * returns null when no session is active.
     *
     * @return HTTP 200 with JSON array of the caller's pins, or HTTP 401 if not logged in
     */
    @Override
    @GetMapping
    public ResponseEntity<List<Pin>> getAllPins() {
        User currentUser = accountFacade.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            List<Pin> pins = pinService.getAllPins(currentUser.getUsername());
            return ResponseEntity.ok(pins);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * FR2 — Update an existing pin by id.
     *
     * Called by React when user edits a pin and clicks Save Changes.
     * {id} in the path identifies which pin to update. PinService applies
     * a partial update — only non-null fields are overwritten so unmodified
     * fields are preserved.
     *
     * Returns the updated Pin as HTTP 200 so React can refresh the marker
     * popup immediately. Update reflected within 1 second per FR2.
     * Returns HTTP 404 if no pin exists with the given id.
     *
     * @param id  path variable — database id of the pin to update
     * @param pin request body — fields to update (nulls are ignored)
     * @return HTTP 200 with updated Pin, or HTTP 404 if pin not found
     */
    @Override
    @PutMapping("/{id}")
    public ResponseEntity<Pin> updatePin(@PathVariable Long id, @RequestBody Pin pin) {
        try {
            return ResponseEntity.ok(pinService.updatePin(id, pin));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * FR3 — Permanently delete a pin by id.
     *
     * Called by React after user confirms the deletion dialog.
     * Returns HTTP 204 No Content on success — no response body needed.
     * React removes the marker from the map on receiving 204.
     * Deletion completes within 1 second per SRS FR3.
     *
     * @ResponseStatus(HttpStatus.NO_CONTENT) ensures Spring sends 204
     * instead of the default 200 for void return types.
     *
     * Returns HTTP 404 if no pin exists with the given id.
     *
     * @param id path variable — database id of the pin to delete
     * @return HTTP 204 on success, HTTP 404 if pin not found
     */
    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePin(@PathVariable Long id) {
        try {
            pinService.deletePin(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * FR11 — Update the privacy level of a pin.
     *
     * Called by React when user selects a new privacy setting.
     * Uses PATCH instead of PUT because only one field is being changed.
     * Jackson deserializes the JSON string body directly to the Privacy enum.
     *
     * Changes apply immediately per SRS FR11 postcondition.
     * Returns the updated Pin so React can update the UI without
     * needing to refetch all pins.
     *
     * Accepted request body values: "PRIVATE", "FRIENDS_ONLY", "PUBLIC"
     *
     * CRITICAL TODO (coordinate with Wilson — NFR4, FR11):
     * Once auth is merged, verify the requesting user owns this pin
     * before applying the change. See PinService.setPinPrivacy() for
     * full details on what Wilson needs to provide.
     *
     * @param id           path variable — database id of the pin
     * @param privacyLevel request body — new Privacy enum value
     * @return HTTP 200 with updated Pin, or HTTP 404 if pin not found
     */
    @Override
    @PatchMapping("/{id}/privacy")
    public ResponseEntity<Pin> setPinPrivacy(
            @PathVariable Long id,
            @RequestBody Privacy privacyLevel) {
        try {
            return ResponseEntity.ok(pinService.setPinPrivacy(id, privacyLevel));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}