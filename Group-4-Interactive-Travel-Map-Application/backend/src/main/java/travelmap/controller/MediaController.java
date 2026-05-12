package travelmap.controller;

import java.net.URLConnection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import travelmap.interfaces.IMediaController;
import travelmap.media.MediaService;
import travelmap.model.Pin;

/**
 * MediaController — Controller layer (MVC) for FR7 Media Upload.
 *
 * Architecture role:
 *   - Receives HTTP requests at /api/media/* and delegates to MediaService.
 *   - Never touches the database or filesystem directly — that's the
 *     service's job.
 *   - Translates MediaService's IllegalArgumentException into the right
 *     HTTP status: 400 Bad Request for validation errors, 404 Not Found
 *     when the message contains "not found" (mirrors MapController).
 *   - Implements IMediaController so tests can depend on the abstraction (DIP).
 *
 * SRS coverage:
 *   FR7 — Upload Media (POST /api/media/{pinId})
 *   FR7 — Serve Media  (GET  /api/media/{filename})
 *   FR7 / FR3 — Remove Media (DELETE /api/media/{pinId})
 *
 * Error handling pattern (mirrors MapController):
 *   - try/catch on every method
 *   - IAE message contains "not found" → ResponseEntity 404
 *   - IAE otherwise                    → ResponseEntity 400
 *   - IllegalStateException (I/O)      → ResponseEntity 500 (rare,
 *                                         indicates disk failure)
 */
@RestController
@RequestMapping("/api/media")
@CrossOrigin(origins = "http://localhost:3000")
public class MediaController implements IMediaController {

    private final MediaService mediaService;

    /**
     * Constructor injection (DIP) of the MediaService dependency.
     *
     * @param mediaService the service that performs the actual upload,
     *                     read, and delete operations
     */
    @Autowired
    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    /**
     * FR7 — Upload a media file and associate it with an existing pin.
     *
     * @param pinId the id of the pin to associate the upload with
     * @param file  the multipart file uploaded by the client (form
     *              parameter name: "file")
     * @return ResponseEntity wrapping the updated Pin:
     *         - 201 Created on success, body is the updated Pin
     *         - 400 Bad Request when validation fails (empty file,
     *               wrong MIME, etc.)
     *         - 404 Not Found when pinId does not exist
     *         - 500 Internal Server Error when disk I/O fails
     */
    @Override
    @PostMapping("/{pinId}")
    public ResponseEntity<Pin> uploadMedia(
            @PathVariable Long pinId,
            @RequestParam("file") MultipartFile file) {
        try {
            Pin updated = mediaService.uploadMedia(pinId, file);
            return ResponseEntity.status(HttpStatus.CREATED).body(updated);
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            // Disk I/O failure — NFR3 reliability concern. 500 is the
            // honest answer; the client can't fix it by retrying with
            // different input.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * FR7 — Serve a previously uploaded media file by filename.
     *
     * The frontend uses this endpoint as the src attribute of img,
     * video, and audio elements. Content-Type is determined from the
     * file extension so the browser can render it inline.
     *
     * @param filename the UUID-based filename (e.g., "abc123.jpg")
     * @return ResponseEntity wrapping the file bytes:
     *         - 200 OK with raw file bytes and correct Content-Type
     *         - 400 Bad Request for malformed filename (path traversal)
     *         - 404 Not Found when file does not exist
     *         - 500 Internal Server Error on disk read failure
     */
    @Override
    @GetMapping("/{filename}")
    public ResponseEntity<byte[]> getMedia(@PathVariable String filename) {
        try {
            byte[] bytes = mediaService.readMedia(filename);
            String contentType = guessContentType(filename);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .body(bytes);
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * FR7 / FR3 — Remove a pin's media file and clear its mediaUrl.
     *
     * Idempotent — calling delete on a pin with no media is not an
     * error and returns 204.
     *
     * @param pinId the id of the pin whose media should be removed
     * @return ResponseEntity:
     *         - 204 No Content on success (or no-op)
     *         - 404 Not Found when pinId does not exist
     *         - 500 Internal Server Error on disk delete failure
     */
    @Override
    @DeleteMapping("/{pinId}")
    public ResponseEntity<Void> deleteMedia(@PathVariable Long pinId) {
        try {
            mediaService.deleteMedia(pinId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    /**
     * Guesses the Content-Type for a given filename based on its
     * extension. Tries URLConnection.guessContentTypeFromName first
     * (built into the JDK, knows common image/video/audio types),
     * falls back to application/octet-stream when unknown.
     *
     * Note: Files.probeContentType is more accurate but depends on
     * platform-specific MIME databases (works on Linux, often returns
     * null on Windows). URLConnection's static map is consistent
     * across platforms.
     *
     * @param filename the filename to inspect
     * @return the guessed MIME type, never null
     */
    private String guessContentType(String filename) {
        String guessed = URLConnection.guessContentTypeFromName(filename);
        if (guessed != null) {
            return guessed;
        }
        return MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }
}
