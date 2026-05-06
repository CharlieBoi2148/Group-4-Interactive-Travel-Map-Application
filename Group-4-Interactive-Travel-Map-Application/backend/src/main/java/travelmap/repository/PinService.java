package travelmap.repository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import travelmap.model.Pin;
import travelmap.model.Privacy;

/**
 * PinService — Service layer (MVC Model).
 *
 * Contains all business logic for pin operations. This is the layer between
 * PinController (which handles HTTP) and PinRepository (which handles the DB).
 * No HTTP-specific code lives here, and no SQL lives here — those concerns
 * belong to PinController and PinRepository respectively.
 *
 * Architecture role:
 *   - PinController receives HTTP requests and delegates here immediately.
 *   - PinService validates, processes, and applies business rules.
 *   - PinRepository is called only from here — never from PinController directly.
 *   - This separation satisfies SRP: each class has exactly one reason to change.
 *     If the database changes, only PinRepository changes.
 *     If business rules change, only PinService changes.
 *     If the API changes, only PinController changes.
 *
 * Dependency injection:
 *   PinRepository is injected via constructor (@Autowired). Constructor
 *   injection is preferred over field injection because it makes dependencies
 *   explicit and makes the class easier to test — in PinServiceTest, you can
 *   pass a mock PinRepository directly into the constructor without needing
 *   a Spring context.
 *
 * SRS coverage:
 *   FR1  — createPin()
 *   FR2  — updatePin()
 *   FR3  — deletePin()
 *   FR4  — getAllPins()
 *   FR11 — setPinPrivacy()
 *
 * NFR coverage:
 *   NFR2 — all operations delegate to JPA which is fast enough for the
 *           2.5 second response time requirement. No heavy computation here.
 *   NFR4 — owner-only enforcement TODO flagged below for Wilson's auth merge.
 */
@Service
public class PinService {

    private final PinRepository pinRepository;

    /**
     * Constructor injection — PinRepository is provided by Spring on startup.
     * Using constructor injection instead of @Autowired field injection so
     * that PinServiceTest can pass a mock repository directly:
     *   PinService service = new PinService(mockRepository);
     *
     * @param pinRepository Spring Data JPA repository for Pin persistence
     */
    @Autowired
    public PinService(PinRepository pinRepository) {
        this.pinRepository = pinRepository;
    }

    /**
     * FR1 — Create and persist a new travel pin.
     *
     * Validates that the minimum required fields are present (locationName
     * and coordinates) before saving. Throws IllegalArgumentException if
     * validation fails — PinController catches this and returns HTTP 400.
     *
     * privacyLevel defaults to PRIVATE if not provided by the client —
     * safe default per NFR4, users must explicitly make pins public.
     *
     * NFR4 — stamps the authenticated user's ownerId onto the pin before
     * saving so that getAllPins() can filter by owner. PinController
     * extracts the username from AccountFacade.getCurrentUser() and passes
     * it here. Without this stamp, findByOwnerId() would never return the
     * pin to its creator.
     *
     * JPA's save() performs an INSERT since the pin has no id yet.
     * The database assigns the id and JPA returns the saved Pin with it.
     * React uses this id to identify the pin for future edit/delete calls.
     *
     * @param pin     Pin object from the HTTP request body
     * @param ownerId username of the authenticated user creating the pin
     * @return the saved Pin with its database-generated id
     * @throws IllegalArgumentException if required fields are missing or ownerId is blank
     */
    public Pin createPin(Pin pin, String ownerId) {
        // Validate required fields per FR1 step 2
        if (pin.getLocationName() == null || pin.getLocationName().trim().isEmpty()) {
            throw new IllegalArgumentException("Location name is required");
        }
        if (pin.getLatitude() == null || pin.getLongitude() == null) {
            throw new IllegalArgumentException("Coordinates are required");
        }

        // Default privacy to PRIVATE if not set — NFR4 safe default
        if (pin.getPrivacyLevel() == null) {
            pin.setPrivacyLevel(Privacy.PRIVATE);
        }

        // NFR4 — stamp owner before persisting so the pin is retrievable by its creator
        if (ownerId == null || ownerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Owner ID is required");
        }
        pin.setOwnerId(ownerId);

        return pinRepository.save(pin);
    }

    /**
     * FR4 — Retrieve all pins belonging to the authenticated user.
     *
     * Called by PinController on GET /api/pins, which MapView.jsx calls
     * on mount to load all pins onto the map. Returns an empty list if
     * no pins exist — never null, so React can safely call .map() on it.
     *
     * Implemented May 2026 — filters by authenticated user's ownerId per NFR4.
     * PinController extracts the username from AccountFacade.getCurrentUser()
     * and passes it here. Only pins stamped with that ownerId are returned.
     *
     * @param ownerId username of the authenticated user
     * @return list of pins owned by the given user
     * @throws IllegalArgumentException if ownerId is null or blank
     */
    public List<Pin> getAllPins(String ownerId) {
        if (ownerId == null || ownerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Owner ID is required");
        }
        return pinRepository.findByOwnerId(ownerId);
    }

    /**
     * FR2 — Update an existing pin's fields.
     *
     * Finds the existing pin by id, applies the updated fields from the
     * request body, then saves. Using this field-by-field approach instead
     * of a full replacement ensures that fields not included in the update
     * request (such as the owner or creation date) are not accidentally wiped.
     *
     * JPA's save() performs an UPDATE when the Pin already has an id.
     * Returns the updated Pin so PinController can send it back to React.
     * Update must complete within 1 second per SRS FR2.
     *
     * Throws IllegalArgumentException if the pin is not found — PinController
     * catches this and returns HTTP 404.
     *
     * TODO (NFR4 — coordinate with Wilson):
     * Once auth is merged, verify the requesting user owns this pin before
     * applying updates. Only the owner or a user with edit permissions should
     * be able to modify a pin per NFR4 security requirements.
     *
     * @param id  the database id of the pin to update
     * @param pin Pin object containing the updated field values
     * @return the updated and saved Pin
     * @throws IllegalArgumentException if no pin exists with the given id
     */
    public Pin updatePin(Long id, Pin pin) {
        Pin existing = pinRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Pin not found: " + id));

        // Apply updates — only overwrite fields that are provided
        if (pin.getLocationName() != null) existing.setLocationName(pin.getLocationName());
        if (pin.getCountry() != null) existing.setCountry(pin.getCountry());
        if (pin.getRegion() != null) existing.setRegion(pin.getRegion());
        if (pin.getVisitDate() != null) existing.setVisitDate(pin.getVisitDate());
        if (pin.getNotes() != null) existing.setNotes(pin.getNotes());
        if (pin.getTripId() != null) existing.setTripId(pin.getTripId());
        if (pin.getPrivacyLevel() != null) existing.setPrivacyLevel(pin.getPrivacyLevel());
        if (pin.getMediaUrl() != null) existing.setMediaUrl(pin.getMediaUrl());

        return pinRepository.save(existing);
    }

    /**
     * FR3 — Permanently delete a pin and all associated metadata.
     *
     * Verifies the pin exists before attempting deletion so a meaningful
     * error is thrown rather than silently doing nothing on a missing id.
     * Deletion must complete within 1 second per SRS FR3.
     *
     * NOTE: "associated metadata" in FR3 includes media files. Currently
     * only the pin record is deleted from the database. Once MediaController
     * is implemented in Week 3, this method must also delete the associated
     * media file from Cloudinary/local storage before deleting the pin.
     *
     * TODO (FR3, FR7 — Week 3):
     * Call MediaService.deleteMedia(pin.getMediaUrl()) here before
     * pinRepository.deleteById(id) once media storage is implemented.
     *
     * TODO (NFR4 — coordinate with Wilson):
     * Once auth is merged, verify the requesting user owns this pin
     * before deleting. Only the owner can delete their own pins per NFR4.
     *
     * @param id the database id of the pin to delete
     * @throws IllegalArgumentException if no pin exists with the given id
     */
    public void deletePin(Long id) {
        // findById().orElseThrow() is used instead of existsById() + deleteById()
        // to avoid two separate database round trips. We fetch the entity once,
        // throw if missing, then delete the object directly. Also sets us up
        // cleanly for the Week 3 TODO where we need the pin object to delete
        // its associated media from Cloudinary before removing the DB record.
        Pin existing = pinRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Pin not found: " + id));
        pinRepository.delete(existing);
    }

    /**
     * FR11 — Update the privacy level of a pin.
     *
     * Finds the pin, applies the new privacy level, and saves.
     * Changes apply immediately per SRS FR11 postcondition.
     * Returns the updated Pin so React can update the UI immediately.
     *
     * Privacy enum values: PRIVATE, FRIENDS_ONLY, PUBLIC.
     * Jackson deserializes the JSON string from the request body into
     * the Privacy enum automatically — no manual parsing needed.
     *
     * CRITICAL TODO (NFR4, FR11 — coordinate with Wilson):
     * Once auth is merged, only the pin owner or a user with explicit edit
     * permissions should be able to change privacy. Without this check any
     * authenticated user can make any pin public — a security violation.
     * Wilson's AccountFacade.getCurrentUser() provides the authenticated user.
     *
     * @param id           the database id of the pin
     * @param privacyLevel the new Privacy enum value to apply
     * @return the updated Pin with the new privacy level
     * @throws IllegalArgumentException if no pin exists with the given id
     */
    public Pin setPinPrivacy(Long id, Privacy privacyLevel) {
        Pin existing = pinRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Pin not found: " + id));

        existing.setPrivacyLevel(privacyLevel);
        return pinRepository.save(existing);
    }
}