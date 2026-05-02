package travelmap.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import travelmap.interfaces.IMediaController;
import travelmap.model.Pin;

/**
 * MediaController — stub satisfying the updated IMediaController contract.
 *
 * All three methods are unimplemented placeholders that allow the project
 * to compile while MediaService and the full FR7 implementation are built
 * in subsequent steps. Each method will be replaced with real logic when
 * MediaService is wired in.
 *
 * Architecture role:
 *   - Receives HTTP requests at /api/media — delegates to MediaService (not yet injected).
 *   - Implements IMediaController so AppController and tests can depend on the
 *     abstraction rather than this concrete class (DIP).
 *
 * SRS coverage (pending full implementation):
 *   FR7 — uploadMedia, getMedia, deleteMedia
 */
@RestController
@RequestMapping("/api/media")
@CrossOrigin(origins = "http://localhost:3000")
public class MediaController implements IMediaController {

    /**
     * FR7 — stub. Full implementation wired in the next step (MediaService injection).
     *
     * @param pinId the id of the pin to associate the media with
     * @param file  the multipart file uploaded by the client
     * @return 501 Not Implemented until MediaService is injected
     */
    @Override
    public ResponseEntity<Pin> uploadMedia(Long pinId, MultipartFile file) {
        throw new UnsupportedOperationException("uploadMedia not yet implemented");
    }

    /**
     * FR7 — stub. Full implementation wired in the next step (MediaService injection).
     *
     * @param filename the UUID-based filename of the media to retrieve
     * @return 501 Not Implemented until MediaService is injected
     */
    @Override
    public ResponseEntity<byte[]> getMedia(String filename) {
        throw new UnsupportedOperationException("getMedia not yet implemented");
    }

    /**
     * FR7 / FR3 — stub. Full implementation wired in the next step (MediaService injection).
     *
     * @param pinId the id of the pin whose media should be removed
     * @return 501 Not Implemented until MediaService is injected
     */
    @Override
    public ResponseEntity<Void> deleteMedia(Long pinId) {
        throw new UnsupportedOperationException("deleteMedia not yet implemented");
    }
}
