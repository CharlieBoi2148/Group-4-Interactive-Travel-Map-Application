package travelmap.interfaces;

import travelmap.model.Pin;
import travelmap.model.Privacy;
import org.springframework.http.ResponseEntity;
import java.util.List;

/**
 * IPinController — Controller interface (MVC).
 *
 * Defines the contract for all pin-related HTTP operations in the system.
 * PinController implements this interface, and all other classes that need
 * to interact with pin operations depend on this interface — not the
 * concrete PinController class. This satisfies two SOLID principles:
 *
 *   ISP (Interface Segregation Principle) — callers only see pin-related
 *   methods. AppController wires this interface, not the concrete class.
 *
 *   DIP (Dependency Inversion Principle) — high-level modules (AppController)
 *   depend on this abstraction, not on PinController directly. This means
 *   PinController can be swapped or mocked in tests without affecting anything
 *   that depends on IPinController.
 *
 * Architecture role:
 *   - This interface sits between AppController and PinController.
 *   - AppController holds a reference of type IPinController.
 *   - PinController provides the concrete implementation.
 *   - PinService is called by PinController — not referenced here.
 *   - The React frontend never knows this interface exists — it only
 *     sends HTTP requests to the endpoints PinController exposes.
 *
 * Return type decision — ResponseEntity<T>:
 *   All methods return ResponseEntity<T> instead of T directly. This is
 *   intentional — the contract includes HTTP status codes, not just data.
 *   ResponseEntity<Pin> gives explicit control over what HTTP status is
 *   returned in both success and error cases. The interface reflects this
 *   because any future implementation of IPinController must also honor
 *   the same HTTP status code contract, not just return raw objects.
 *
 * Method signature changes from skeleton:
 *   - Return types changed from void/Pin/List<Pin> to ResponseEntity<T>
 *     so HTTP status codes are part of the interface contract.
 *   - Parameter types changed from Object to Pin so Jackson can deserialize
 *     incoming JSON request bodies directly into Pin objects via @RequestBody.
 *   - handleViewPins() renamed to getAllPins() to match REST conventions.
 *   - pinId changed from String to Long to match the updated Pin.id type.
 *   - setPinPrivacy uses Privacy enum directly — Jackson deserializes JSON
 *     strings to enum values automatically, no manual conversion needed.
 *
 * SRS coverage:
 *   FR1  — createPin()
 *   FR2  — updatePin()
 *   FR3  — deletePin()
 *   FR4  — getAllPins()
 *   FR11 — setPinPrivacy()
 */
public interface IPinController {

    /**
     * FR1 — Create a new travel pin.
     * Called when React POSTs to /api/pins with pin data from PinForm.jsx.
     * Returns HTTP 201 with the saved Pin on success so React can add the
     * marker immediately with the correct database-generated id.
     * Returns HTTP 400 if required fields are missing.
     *
     * @param pin Pin object deserialized from the JSON request body
     * @return HTTP 201 with saved Pin, or HTTP 400 if validation fails
     */
    ResponseEntity<Pin> createPin(Pin pin);

    /**
     * FR4 — Retrieve all pins for the current user.
     * Called when MapView.jsx mounts — loads all pins onto the map.
     * Returns empty list if no pins exist — never null.
     *
     * CRITICAL TODO (coordinate with Wilson — FR4, NFR4):
     * This currently returns ALL pins in the database regardless of owner.
     * Once Wilson's auth branch is merged to dev, this must be updated to
     * filter pins by the authenticated user's id. Without this, every user
     * sees every other user's pins — a serious security and privacy violation.
     * Wilson's AccountFacade.getCurrentUser() will provide the authenticated
     * user. PinService.getAllPins() will need a userId parameter added.
     *
     * @return HTTP 200 with JSON array of all pins
     */
    ResponseEntity<List<Pin>> getAllPins();

    /**
     * FR2 — Update an existing pin.
     * Called when React PUTs to /api/pins/{id} after the user edits a pin.
     * PinService applies a partial update — only non-null fields overwritten.
     * Update must be reflected within 1 second per SRS FR2.
     *
     * @param id  the database id of the pin to update
     * @param pin Pin object with updated fields from the edit form
     * @return HTTP 200 with updated Pin, or HTTP 404 if pin not found
     */
    ResponseEntity<Pin> updatePin(Long id, Pin pin);

    /**
     * FR3 — Delete a pin and all its associated metadata permanently.
     * Called when React DELETEs /api/pins/{id} after the confirmation dialog.
     * Returns HTTP 204 No Content — no body needed, React removes the marker.
     * Deletion must complete within 1 second per SRS FR3.
     *
     * @param id the database id of the pin to delete
     * @return HTTP 204 on success, or HTTP 404 if pin not found
     */
    ResponseEntity<Void> deletePin(Long id);

    /**
     * FR11 — Set the privacy level of a pin.
     * Called when user selects PRIVATE, FRIENDS_ONLY, or PUBLIC in the UI.
     * Changes apply immediately per SRS FR11 postcondition.
     * Returns the updated Pin so React can update the UI without refetching.
     *
     * Privacy enum is used instead of String — Spring's Jackson library
     * deserializes JSON strings ("PRIVATE", "FRIENDS_ONLY", "PUBLIC") into
     * the Privacy enum automatically. Type-safe, no manual conversion needed.
     *
     * CRITICAL TODO (coordinate with Wilson — NFR4, FR11):
     * Once auth is merged, verify that only the pin owner OR a user with
     * explicit edit permissions can call this method. Without that check
     * any authenticated user can change any pin's privacy level — a direct
     * violation of NFR4 and FR11. Wilson's AccountFacade.getCurrentUser()
     * provides the authenticated user for comparison against the pin owner.
     *
     * @param id           the database id of the pin
     * @param privacyLevel the new Privacy enum value
     * @return HTTP 200 with updated Pin, or HTTP 404 if pin not found
     */
    ResponseEntity<Pin> setPinPrivacy(Long id, Privacy privacyLevel);
}