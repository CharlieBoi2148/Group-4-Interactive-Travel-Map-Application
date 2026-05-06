package travelmap.interfaces;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import travelmap.model.Pin;

/**
 * IMediaController — interface contract for the FR7 Media Upload REST layer.
 *
 * Architecture role:
 *   - This interface defines the public contract for MediaController.
 *   - MediaController implements this interface (LSP — substitutability).
 *   - AppController and tests depend on this interface, not on the concrete
 *     MediaController class (DIP — depend on abstractions).
 *   - Only methods the consumer needs are exposed (ISP — interface segregation).
 *
 * SRS coverage:
 *   FR7 — Upload Media:
 *     - User selects "Upload Media"
 *     - User selects photo, video, or audio file
 *     - System uploads file and associates it with the pin
 *     - Media preview is displayed within 2 seconds
 *
 * Why every method returns ResponseEntity<T>:
 *   - Mirrors the established pattern in IPinController and IMapController.
 *   - HTTP status codes are part of the contract, not an implementation
 *     detail — 200 OK, 201 Created, 400 Bad Request, 404 Not Found, 413
 *     Payload Too Large all carry semantic meaning the frontend acts on.
 *   - Spring's MultipartFile is preferred over Object so the contract is
 *     explicit about expecting an HTTP multipart upload, not arbitrary data.
 *
 * Related files:
 *   - MediaController        (implements this interface)
 *   - MediaService           (business logic, called by MediaController)
 *   - Pin.mediaUrl           (the field this controller updates on upload)
 *   - mediaService.js        (frontend service that POSTs to these endpoints)
 */
public interface IMediaController {

    /**
     * FR7 — Upload a media file and associate it with an existing pin.
     *
     * The uploaded file is saved to disk under the configured upload
     * directory with a UUID-based filename to prevent collisions and
     * path traversal. The pin's mediaUrl field is updated to point to
     * the new file's public URL ("/api/media/{filename}"). The updated
     * Pin is returned so the frontend can refresh its local state in
     * a single round trip.
     *
     * @param pinId the id of the pin to associate the media with —
     *              must reference an existing pin
     * @param file  the multipart file uploaded by the client — must be
     *              non-empty and of MIME type image/*, video/*, or audio/*
     * @return ResponseEntity wrapping the updated Pin:
     *         - 201 Created with updated Pin in body — upload succeeded
     *         - 400 Bad Request — file empty, wrong type, or pin id invalid
     *         - 404 Not Found — pin id does not exist
     *         - 413 Payload Too Large — file exceeds max-file-size
     *           (returned by Spring's MultipartResolver before reaching
     *            the controller method)
     */
    ResponseEntity<Pin> uploadMedia(Long pinId, MultipartFile file);

    /**
     * FR7 — Serve a previously uploaded media file by filename.
     *
     * The frontend uses this endpoint as the src attribute of img, video,
     * and audio elements. The response body is the raw file bytes with
     * the correct Content-Type header set explicitly by the implementation
     * (e.g. via Files.probeContentType), so the browser can render it
     * natively. The implementation must set Content-Type — Spring does not
     * detect it automatically from byte[].
     *
     * @param filename the UUID-based filename returned from a previous
     *                 upload (e.g., "abc123.jpg")
     * @return ResponseEntity wrapping the file bytes:
     *         - 200 OK with file content and explicit Content-Type header
     *         - 404 Not Found — filename does not exist on disk
     */
    ResponseEntity<byte[]> getMedia(String filename);

    /**
     * FR7 / FR3 — Remove a pin's media file from disk and clear its
     * mediaUrl field.
     *
     * Called by:
     *   - The user explicitly clicks "Remove Media" on the edit form
     *   - PinService.deletePin() before deleting the pin itself, so
     *     orphaned files are not left on disk (NFR3 reliability)
     *
     * Idempotent — calling delete on a pin with no media is not an error,
     * it returns 204 with no action taken.
     *
     * @param pinId the id of the pin whose media should be removed
     * @return ResponseEntity:
     *         - 204 No Content — media removed (or pin had no media)
     *         - 404 Not Found — pin id does not exist
     *
     * The updated Pin is not returned because the only field that
     * changed is mediaUrl=null, which the frontend can set locally.
     */
    ResponseEntity<Void> deleteMedia(Long pinId);
}
