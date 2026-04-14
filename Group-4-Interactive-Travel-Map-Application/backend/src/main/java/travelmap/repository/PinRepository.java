package travelmap.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import travelmap.model.Pin;
import travelmap.model.Privacy;

/**
 * PinRepository — Repository layer (MVC Model).
 *
 * This is the ONLY class that directly accesses the database for pin data.
 * It extends JpaRepository which provides all standard CRUD operations
 * for free — no SQL or implementation code needed. Spring Data JPA
 * generates the implementation automatically at runtime.
 *
 * Architecture role:
 *   - Sits at the bottom of the backend stack: Controller → Service → Repository → DB
 *   - PinService is the only class that calls PinRepository directly.
 *   - PinController never calls PinRepository — it always goes through PinService.
 *   - This separation means business logic stays in PinService and database
 *     access stays here. Satisfies SRP (Single Responsibility Principle).
 *
 * JpaRepository<Pin, Long> provides these methods for free:
 *   - save(pin)          → INSERT or UPDATE
 *   - findById(id)       → SELECT WHERE id = ?
 *   - findAll()          → SELECT all pins
 *   - deleteById(id)     → DELETE WHERE id = ?
 *   - existsById(id)     → SELECT COUNT WHERE id = ?
 *   - count()            → SELECT COUNT(*)
 *
 * Custom query methods below are derived from their method names —
 * Spring Data JPA parses the method name and generates the SQL automatically.
 * No @Query annotation or SQL string needed.
 *
 * Database:
 *   Currently H2 in-memory — data persists across browser refreshes but
 *   resets when Spring Boot restarts. Swap to PostgreSQL before final
 *   submission by updating application.properties. No changes needed here.
 *
 * SRS coverage:
 *   FR1  — save() used by PinService.createPin()
 *   FR2  — save() used by PinService.updatePin() (JPA merges if id exists)
 *   FR3  — deleteById() used by PinService.deletePin()
 *   FR4  — findAll() used by PinService.getAllPins()
 *   FR11 — findByPrivacyLevel() used to filter pins by visibility
 *
 * TODO (FR5 — coordinate with Gage):
 *   Once Gage's Trip class is merged to dev, add:
 *   List<Pin> findByTripId(Long tripId)
 *   This retrieves all pins belonging to a specific trip.
 *
 * TODO (FR4, NFR4 — coordinate with Wilson):
 *   Once Wilson's auth is merged, add:
 *   List<Pin> findByOwnerId(Long ownerId)
 *   This filters pins by the authenticated user so users only see their own pins.
 *   Wilson's AccountFacade.getCurrentUser() provides the ownerId.
 *   Also requires adding an ownerId field to the Pin model.
 */
@Repository
public interface PinRepository extends JpaRepository<Pin, Long> {

    /**
     * FR11 — Find all pins with a specific privacy level.
     * Used when determining which pins are visible to other users.
     * Spring Data JPA generates: SELECT * FROM pin WHERE privacy_level = ?
     *
     * Example usage in PinService:
     *   List<Pin> publicPins = pinRepository.findByPrivacyLevel(Privacy.PUBLIC);
     *
     * @param privacyLevel the privacy level to filter by
     * @return list of pins matching the given privacy level
     */
    List<Pin> findByPrivacyLevel(Privacy privacyLevel);

    /**
     * FR9 — Find all pins containing the keyword in their location name or notes.
     * Used by SearchController when a user enters a keyword search.
     * Spring Data JPA generates:
     *   SELECT * FROM pin WHERE location_name LIKE %keyword%
     *
     * TODO (coordinate with Gage — FR9):
     * Gage's SearchController will call this via PinService.
     * Consider expanding to search notes field too — may need a
     * @Query annotation for multi-field search:
     *   SELECT p FROM Pin p WHERE p.locationName LIKE %:keyword%
     *   OR p.notes LIKE %:keyword%
     *
     * @param keyword the search term
     * @return list of pins whose location name contains the keyword
     */
    List<Pin> findByLocationNameContainingIgnoreCase(String keyword);
}