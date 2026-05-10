package travelmap.model;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * Pin — Model layer (MVC).
 *
 * Represents a single travel pin placed by a user on the interactive map.
 * This is a JPA Entity — Spring Data JPA maps it directly to a "pin" table
 * in the database. H2 creates this table automatically on startup.
 * When the project switches to PostgreSQL, no changes are needed here —
 * only application.properties needs to be updated.
 *
 * Architecture role:
 *   - This class is the Model. It holds data only — no business logic.
 *   - PinController receives HTTP requests and delegates to PinService.
 *   - PinService contains business logic and calls PinRepository.
 *   - PinRepository (Spring Data JPA) persists Pin objects to the database.
 *   - The React frontend never touches this class directly — it communicates
 *     via JSON over HTTP through PinController's REST endpoints.
 *
 * SRS coverage:
 *   FR1  — locationName, country, region, visitDate, notes, latitude, longitude
 *   FR2  — all fields are mutable via PinService.updatePin()
 *   FR3  — permanent deletion handled by PinRepository.deleteById()
 *   FR4  — retrieved via PinRepository.findAll()
 *   FR6  — latitude and longitude displayed as markers on the Leaflet map
 *   FR7  — mediaUrl stores the URL of uploaded media (Cloudinary/local)
 *   FR8  — latitude and longitude used for distance calculations in MapService
 *   FR11 — privacyLevel controls visibility (PRIVATE / FRIENDS_ONLY / PUBLIC)
 *
 * TODO (FR5 — Organize Trip):
 *   tripId is currently a simple foreign-key value.
 *   Upgrade to @ManyToOne mapping once Trip/Pin relationship is finalized.
 */
@Entity
public class Pin {

    /**
     * Primary key — auto-incremented by the database.
     * H2 and PostgreSQL both support IDENTITY strategy.
     * Replaces the original String pinId from the skeleton —
     * Long is standard for JPA auto-generated keys.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FR1 — user-entered fields from PinForm.jsx
    private String locationName; // required — PinController validates before saving
    private String country;      // optional per SRS but collected in FR1 step 2
    private String region;       // optional per SRS but collected in FR1 step 2
    private LocalDate visitDate; // FR1 step 2 — stored as LocalDate, not LocalDateTime
    private String notes;        // FR1 step 4 — optional

    // FR6, FR8 — coordinates captured from Leaflet map click event
    // Sent from React as { latitude, longitude } in the POST /api/pins request body
    private Double latitude;
    private Double longitude;

    // FR5 — optional trip assignment for grouping pins under a trip.
    private Long tripId;

    /**
     * NFR4, FR11 — ID of the user who owns this pin.
     *
     * Used to enforce owner-only access control. Only the owner can edit,
     * delete, or change the privacy of their own pins. Also used by
     * PinRepository.findByOwnerId() to scope GET /api/pins so that users
     * only see their own pins (fixes NFR4 violation where getAllPins
     * previously returned every user's pins).
     *
     * Stored as String to match Gage's Trip.ownerId and to remain flexible
     * regardless of whether Wilson's User entity uses Long IDs, UUIDs, or
     * usernames. If User.id is Long, conversion is trivial at the auth
     * layer via String.valueOf(user.getId()).
     *
     * Currently nullable — populated by AuthController via Spring Security
     * once Wilson's auth layer lands. Until then, this field stays null
     * and getAllPins() continues to use findAll() unchanged.
     */
    private String ownerId;

    /**
     * FR11 — privacy setting for this pin.
     * Stored as a string in the database ("PRIVATE", "FRIENDS_ONLY", "PUBLIC").
     * EnumType.STRING is used instead of EnumType.ORDINAL so that the database
     * column is human-readable and safe if enum order changes in future.
     *
     * Defaults to PRIVATE — pins are private unless the user explicitly changes.
     * Access control is enforced in PinService, not here.
     *
     * TODO: Privacy enum is defined in travelmap.model.Privacy.
     * Ensure Privacy.java is on the classpath — it was part of the skeleton.
     */
    @Enumerated(EnumType.STRING)
    private Privacy privacyLevel = Privacy.PRIVATE;

    /**
     * FR7 — URL of the media file associated with this pin.
     * The actual file is stored in Cloudinary or local storage.
     * Only the URL string is saved here, not the file itself.
     */
    private String mediaUrl;

    /**
     * Partial-update convention for {@code PUT /api/pins/{id}}: JSON {@code "tripId": -1}
     * clears assignment (unassigned pin). Matches {@code NO_TRIP_ASSIGNMENT} in
     * {@code pinService.js}. Not persisted — the saved entity uses {@code null}.
     */
    public static final long NO_TRIP_ASSIGNMENT = -1L;

    // ── Getters and Setters ───────────────────────────────────────────────────
    // Required by JPA and Spring's JSON serialization (Jackson).
    // Jackson uses these to convert Pin objects to/from JSON for the REST API.

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public LocalDate getVisitDate() { return visitDate; }
    public void setVisitDate(LocalDate visitDate) { this.visitDate = visitDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Long getTripId() { return tripId; }
    public void setTripId(Long tripId) { this.tripId = tripId; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public Privacy getPrivacyLevel() { return privacyLevel; }
    public void setPrivacyLevel(Privacy privacyLevel) { this.privacyLevel = privacyLevel; }

    public String getMediaUrl() { return mediaUrl; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }
}