package travelmap.media;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import travelmap.model.Pin;
import travelmap.repository.PinRepository;

/**
 * MediaService — Service layer (MVC) for FR7 Media Upload.
 *
 * Architecture role:
 *   - This class is the Service. It contains all business logic for media
 *     upload, retrieval, and deletion.
 *   - MediaController receives HTTP requests and delegates to this class.
 *   - This class calls PinRepository to update the associated Pin's
 *     mediaUrl field, but never accesses HTTP concepts (no ResponseEntity,
 *     no @RequestMapping, no MultipartFile validation beyond what's
 *     needed for business logic).
 *   - File I/O is handled here. The controller never touches the disk
 *     directly.
 *
 * Placement:
 *   - Lives in travelmap.media to group with MapAPIClient's travelmap.map
 *     pattern. Final-sprint cleanup may move services to travelmap.service.
 *   - Annotated @Service (not @Component) — addresses the tech debt flagged
 *     for MapAPIClient.
 *
 * SRS coverage:
 *   FR7 — Upload Media:
 *     - Validates file is non-empty and of an allowed MIME type
 *     - Generates a UUID-based filename to prevent collisions and
 *       path-traversal attacks (NFR4 security)
 *     - Writes file bytes to the configured upload directory
 *     - Updates the associated Pin's mediaUrl field via PinRepository
 *   FR3 — Delete Pin:
 *     - deleteMedia() is called by PinService before deleting a pin so
 *       orphaned files are not left on disk (NFR3 reliability)
 *   NFR2 Performance:
 *     - File save and pin update both occur in a single method call;
 *       2-second preview budget is met as long as the file is under
 *       the 10MB multipart limit.
 *   NFR3 Reliability:
 *     - All I/O exceptions are wrapped in IllegalStateException so the
 *       caller sees a clear failure rather than a silent partial write.
 *   NFR4 Security:
 *     - User-supplied filenames are NEVER used. UUID-based filenames
 *       prevent path traversal (e.g. "../../etc/passwd") and collisions.
 *     - MIME type whitelist (image/*, video/*, audio/*) prevents arbitrary
 *       executable upload.
 */
@Service
public class MediaService {

    /**
     * MIME-type whitelist. Files are accepted only if their reported
     * Content-Type begins with one of these prefixes.
     *
     * Note: Content-Type is reported by the client and can be spoofed.
     * For a school project this is sufficient; production systems should
     * also verify magic bytes (e.g. via Apache Tika).
     */
    private static final Set<String> ALLOWED_PREFIXES = Set.of(
            "image/", "video/", "audio/");

    private final PinRepository pinRepository;
    private final Path uploadDir;

    /**
     * Constructor injection (DIP). Spring resolves PinRepository via
     * autowiring, and the upload directory path comes from
     * application.properties via @Value.
     *
     * @param pinRepository  the JPA repository for Pin entities
     * @param uploadDirValue the upload directory path from
     *                       application.properties (travelmap.media.upload-dir)
     */
    @Autowired
    public MediaService(
            PinRepository pinRepository,
            @Value("${travelmap.media.upload-dir}") String uploadDirValue) {
        this.pinRepository = pinRepository;
        this.uploadDir = Paths.get(uploadDirValue).toAbsolutePath().normalize();
    }

    /**
     * FR7 — Upload a media file and associate it with a pin.
     *
     * Validation order (fail fast):
     *   1. file must be non-null and non-empty
     *   2. file's content-type must start with an allowed prefix
     *   3. pin with the given id must exist
     *
     * On success:
     *   - A unique filename is generated using UUID + the original file
     *     extension (or empty if the original had no extension).
     *   - The file's bytes are written to disk under uploadDir.
     *   - The pin's mediaUrl is set to "/api/media/{filename}" — the
     *     public URL the frontend will use as an img/video/audio src.
     *   - The updated Pin is saved and returned.
     *
     * @param pinId the id of the pin to associate the upload with
     * @param file  the multipart file to upload
     * @return the updated Pin with mediaUrl populated
     * @throws IllegalArgumentException if file is null/empty, MIME type
     *         is not allowed, or pin id does not exist (the message
     *         contains "not found" for the pin-missing case so the
     *         controller can map to 404)
     * @throws IllegalStateException if disk I/O fails — wraps the
     *         underlying IOException so callers do not need a checked
     *         exception declaration (NFR3 reliability)
     */
    public Pin uploadMedia(Long pinId, MultipartFile file) {
        validateFile(file);
        Pin pin = pinRepository.findById(pinId).orElseThrow(
                () -> new IllegalArgumentException("Pin not found: " + pinId));

        String filename = generateFilename(file.getOriginalFilename());
        Path target = uploadDir.resolve(filename);

        try {
            Files.createDirectories(uploadDir);
            file.transferTo(target);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to save media file for pin " + pinId, e);
        }

        pin.setMediaUrl("/api/media/" + filename);
        return pinRepository.save(pin);
    }

    /**
     * FR7 — Read the bytes of a previously uploaded media file.
     *
     * @param filename the UUID-based filename returned by uploadMedia
     * @return the file's raw bytes
     * @throws IllegalArgumentException if filename is null, blank, or
     *         contains path-traversal characters; or if the file does
     *         not exist on disk (message contains "not found" so the
     *         controller can map to 404)
     * @throws IllegalStateException if disk I/O fails during read
     *         (NFR3 reliability)
     */
    public byte[] readMedia(String filename) {
        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException("Filename must not be blank");
        }
        if (filename.contains("/") || filename.contains("\\")
                || filename.contains("..")) {
            throw new IllegalArgumentException(
                    "Filename contains illegal characters: " + filename);
        }

        Path target = uploadDir.resolve(filename).normalize();
        // Defense in depth — even after the character check, ensure the
        // resolved path stays inside uploadDir.
        if (!target.startsWith(uploadDir)) {
            throw new IllegalArgumentException(
                    "Filename resolves outside upload directory: " + filename);
        }
        if (!Files.exists(target)) {
            throw new IllegalArgumentException(
                    "Media file not found: " + filename);
        }

        try {
            return Files.readAllBytes(target);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to read media file: " + filename, e);
        }
    }

    /**
     * FR7 / FR3 — Remove a pin's media file from disk and clear the
     * Pin.mediaUrl field.
     *
     * Idempotent — safe to call on a pin with no media. The pin's
     * mediaUrl is set to null even if no file existed on disk.
     *
     * @param pinId the id of the pin whose media should be removed
     * @throws IllegalArgumentException if pin id does not exist
     *         (message contains "not found" so the controller can
     *         map to 404)
     * @throws IllegalStateException if disk I/O fails during the
     *         file delete (NFR3 reliability)
     */
    public void deleteMedia(Long pinId) {
        Pin pin = pinRepository.findById(pinId).orElseThrow(
                () -> new IllegalArgumentException("Pin not found: " + pinId));

        String mediaUrl = pin.getMediaUrl();
        if (mediaUrl != null && mediaUrl.startsWith("/api/media/")) {
            String filename = mediaUrl.substring("/api/media/".length());
            Path target = uploadDir.resolve(filename).normalize();
            // Defense in depth — refuse to delete anything outside uploadDir.
            if (target.startsWith(uploadDir)) {
                try {
                    Files.deleteIfExists(target);
                } catch (IOException e) {
                    throw new IllegalStateException(
                            "Failed to delete media file: " + filename, e);
                }
            }
        }

        pin.setMediaUrl(null);
        pinRepository.save(pin);
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    /**
     * Validates a multipart file against the FR7 acceptance criteria.
     *
     * @param file the file to validate
     * @throws IllegalArgumentException if the file is null, empty, or
     *         of a disallowed MIME type
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty");
        }
        String contentType = file.getContentType();
        if (contentType == null) {
            throw new IllegalArgumentException(
                    "File content-type is missing");
        }
        boolean allowed = ALLOWED_PREFIXES.stream()
                .anyMatch(contentType::startsWith);
        if (!allowed) {
            throw new IllegalArgumentException(
                    "File type not allowed: " + contentType
                    + " (must be image/*, video/*, or audio/*)");
        }
    }

    /**
     * Builds a unique, safe filename for an uploaded file.
     *
     * Pattern: "{uuid}{.ext}" where ext is the lowercase extension from
     * the original filename (or empty if the original had none).
     *
     * Examples:
     *   "vacation.JPG"  -> "550e8400-e29b-41d4-a716-446655440000.jpg"
     *   "noext"         -> "550e8400-e29b-41d4-a716-446655440000"
     *   null            -> "550e8400-e29b-41d4-a716-446655440000"
     *
     * The original filename is NEVER reused — UUID prevents collisions
     * and removes any user-controlled path component (NFR4 security).
     *
     * @param originalFilename the filename reported by the client (may
     *                         be null, may contain path separators on
     *                         some browsers)
     * @return a UUID-based filename safe for filesystem use
     */
    private String generateFilename(String originalFilename) {
        String uuid = UUID.randomUUID().toString();
        if (originalFilename == null || originalFilename.isBlank()) {
            return uuid;
        }
        int dot = originalFilename.lastIndexOf('.');
        if (dot < 0 || dot == originalFilename.length() - 1) {
            return uuid;
        }
        String ext = originalFilename.substring(dot + 1).toLowerCase();
        // Strip anything that isn't a-z0-9 from the extension to defend
        // against odd Unicode or path traversal in the extension itself.
        ext = ext.replaceAll("[^a-z0-9]", "");
        return ext.isEmpty() ? uuid : uuid + "." + ext;
    }
}
