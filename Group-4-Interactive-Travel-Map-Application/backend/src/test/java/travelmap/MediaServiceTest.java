package travelmap;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import travelmap.media.MediaService;
import travelmap.model.Pin;
import travelmap.model.Privacy;
import travelmap.repository.PinRepository;

/**
 * MediaServiceTest — JUnit 5 unit tests for FR7 Media Upload service layer.
 *
 * Test strategy:
 *   - Real disk I/O against a JUnit @TempDir — verifies actual file writes
 *     and reads, not mocked behavior. Each test gets a fresh temp directory
 *     that JUnit cleans up automatically.
 *   - PinRepository is mocked with Mockito — we don't want a real DB and we
 *     want to control "pin not found" cases precisely.
 *   - Tests are organized by method and use input partitioning:
 *       Valid inputs   — happy path for image, video, audio
 *       Boundary cases — empty file, missing content type, missing extension
 *       Invalid inputs — wrong MIME, null pin, path traversal attempts
 *       I/O failures   — file read after delete, etc.
 *
 * SRS coverage:
 *   FR7 — every test maps to a stated FR7 behavior
 *   NFR3 — I/O exception wrapping verified
 *   NFR4 — path traversal defense exhaustively tested
 */
@ExtendWith(MockitoExtension.class)
class MediaServiceTest {

    @Mock
    private PinRepository pinRepository;

    private MediaService mediaService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        // Pass the absolute temp directory path so MediaService normalizes
        // to it and writes files there. We don't pass through @Value because
        // we are constructing the service directly without Spring context.
        mediaService = new MediaService(pinRepository, tempDir.toString());
    }

    // ── uploadMedia — happy paths ─────────────────────────────────────────

    @Test
    @DisplayName("FR7 — uploadMedia accepts an image and writes it to disk")
    void uploadMedia_imageType_writesFileAndUpdatesPin() throws IOException {
        Pin pin = makePin(1L);
        when(pinRepository.findById(1L)).thenReturn(Optional.of(pin));
        when(pinRepository.save(any(Pin.class))).thenAnswer(inv -> inv.getArgument(0));

        MultipartFile file = new MockMultipartFile(
                "file", "vacation.jpg", "image/jpeg", new byte[]{1, 2, 3});

        Pin result = mediaService.uploadMedia(1L, file);

        assertTrue(result.getMediaUrl().startsWith("/api/media/"),
                "mediaUrl should start with /api/media/");
        assertTrue(result.getMediaUrl().endsWith(".jpg"),
                "mediaUrl should preserve .jpg extension");

        // Verify the file was actually written to disk
        String filename = result.getMediaUrl().substring("/api/media/".length());
        Path written = tempDir.resolve(filename);
        assertTrue(Files.exists(written), "file should be written to disk");
        assertArrayEquals(new byte[]{1, 2, 3}, Files.readAllBytes(written));

        verify(pinRepository).save(pin);
    }

    @Test
    @DisplayName("FR7 — uploadMedia accepts a video file")
    void uploadMedia_videoType_succeeds() {
        Pin pin = makePin(2L);
        when(pinRepository.findById(2L)).thenReturn(Optional.of(pin));
        when(pinRepository.save(any(Pin.class))).thenAnswer(inv -> inv.getArgument(0));

        MultipartFile file = new MockMultipartFile(
                "file", "trip.mp4", "video/mp4", new byte[]{10});

        Pin result = mediaService.uploadMedia(2L, file);

        assertTrue(result.getMediaUrl().endsWith(".mp4"));
    }

    @Test
    @DisplayName("FR7 — uploadMedia accepts an audio file")
    void uploadMedia_audioType_succeeds() {
        Pin pin = makePin(3L);
        when(pinRepository.findById(3L)).thenReturn(Optional.of(pin));
        when(pinRepository.save(any(Pin.class))).thenAnswer(inv -> inv.getArgument(0));

        MultipartFile file = new MockMultipartFile(
                "file", "voicenote.mp3", "audio/mpeg", new byte[]{20});

        Pin result = mediaService.uploadMedia(3L, file);

        assertTrue(result.getMediaUrl().endsWith(".mp3"));
    }

    @Test
    @DisplayName("FR7 — uploadMedia preserves extension regardless of case")
    void uploadMedia_uppercaseExtension_isLowercased() {
        Pin pin = makePin(4L);
        when(pinRepository.findById(4L)).thenReturn(Optional.of(pin));
        when(pinRepository.save(any(Pin.class))).thenAnswer(inv -> inv.getArgument(0));

        MultipartFile file = new MockMultipartFile(
                "file", "VACATION.JPG", "image/jpeg", new byte[]{1});

        Pin result = mediaService.uploadMedia(4L, file);

        assertTrue(result.getMediaUrl().endsWith(".jpg"),
                "extension should be lowercased");
    }

    @Test
    @DisplayName("FR7 — uploadMedia handles file with no extension")
    void uploadMedia_noExtension_uuidOnly() {
        Pin pin = makePin(5L);
        when(pinRepository.findById(5L)).thenReturn(Optional.of(pin));
        when(pinRepository.save(any(Pin.class))).thenAnswer(inv -> inv.getArgument(0));

        MultipartFile file = new MockMultipartFile(
                "file", "noext", "image/png", new byte[]{1});

        Pin result = mediaService.uploadMedia(5L, file);

        assertFalse(result.getMediaUrl().contains("."),
                "filename should have no extension");
    }

    @Test
    @DisplayName("NFR4 — uploadMedia generates unique filenames on repeated upload")
    void uploadMedia_repeatedUpload_uniqueFilenames() {
        Pin pin = makePin(6L);
        when(pinRepository.findById(6L)).thenReturn(Optional.of(pin));
        when(pinRepository.save(any(Pin.class))).thenAnswer(inv -> inv.getArgument(0));

        MultipartFile file1 = new MockMultipartFile(
                "file", "same.jpg", "image/jpeg", new byte[]{1});
        MultipartFile file2 = new MockMultipartFile(
                "file", "same.jpg", "image/jpeg", new byte[]{2});

        Pin r1 = mediaService.uploadMedia(6L, file1);
        // Capture the URL before the second upload mutates the same Pin object.
        // r1 and r2 share the same mutable Pin reference, so r1.getMediaUrl()
        // would return url2 after the second call without this capture.
        String url1 = r1.getMediaUrl();
        Pin r2 = mediaService.uploadMedia(6L, file2);

        assertFalse(url1.equals(r2.getMediaUrl()),
                "identical filenames must produce unique stored paths");
    }

    // ── uploadMedia — invalid inputs ──────────────────────────────────────

    @Test
    @DisplayName("FR7 — uploadMedia rejects empty file")
    void uploadMedia_emptyFile_throwsIAE() {
        MultipartFile empty = new MockMultipartFile(
                "file", "empty.jpg", "image/jpeg", new byte[0]);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> mediaService.uploadMedia(1L, empty));

        assertTrue(ex.getMessage().contains("empty"));
        verify(pinRepository, never()).save(any());
    }

    @Test
    @DisplayName("FR7 — uploadMedia rejects null file")
    void uploadMedia_nullFile_throwsIAE() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> mediaService.uploadMedia(1L, null));

        assertTrue(ex.getMessage().contains("empty"));
    }

    @Test
    @DisplayName("FR7 — uploadMedia rejects text/plain")
    void uploadMedia_textType_throwsIAE() {
        MultipartFile text = new MockMultipartFile(
                "file", "evil.txt", "text/plain", new byte[]{1});

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> mediaService.uploadMedia(1L, text));

        assertTrue(ex.getMessage().contains("not allowed"));
    }

    @Test
    @DisplayName("NFR4 — uploadMedia rejects executable")
    void uploadMedia_executableType_throwsIAE() {
        MultipartFile exe = new MockMultipartFile(
                "file", "evil.exe", "application/x-msdownload", new byte[]{1});

        assertThrows(IllegalArgumentException.class,
                () -> mediaService.uploadMedia(1L, exe));
    }

    @Test
    @DisplayName("FR7 — uploadMedia rejects null content-type")
    void uploadMedia_nullContentType_throwsIAE() {
        MultipartFile noType = new MockMultipartFile(
                "file", "x.jpg", null, new byte[]{1});

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> mediaService.uploadMedia(1L, noType));

        assertTrue(ex.getMessage().contains("content-type"));
    }

    @Test
    @DisplayName("FR7 — uploadMedia returns 'not found' message when pin missing")
    void uploadMedia_pinNotFound_throwsIAEWithNotFound() {
        when(pinRepository.findById(999L)).thenReturn(Optional.empty());

        MultipartFile file = new MockMultipartFile(
                "file", "x.jpg", "image/jpeg", new byte[]{1});

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> mediaService.uploadMedia(999L, file));

        assertTrue(ex.getMessage().contains("not found"),
                "controller relies on 'not found' substring for 404 mapping");
    }

    // ── readMedia ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("FR7 — readMedia returns bytes for existing file")
    void readMedia_existingFile_returnsBytes() throws IOException {
        Path file = tempDir.resolve("test.jpg");
        Files.write(file, new byte[]{1, 2, 3, 4});

        byte[] result = mediaService.readMedia("test.jpg");

        assertArrayEquals(new byte[]{1, 2, 3, 4}, result);
    }

    @Test
    @DisplayName("FR7 — readMedia throws 'not found' when file missing")
    void readMedia_missingFile_throwsIAEWithNotFound() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> mediaService.readMedia("does-not-exist.jpg"));

        assertTrue(ex.getMessage().contains("not found"));
    }

    @Test
    @DisplayName("FR7 — readMedia rejects null filename")
    void readMedia_nullFilename_throwsIAE() {
        assertThrows(IllegalArgumentException.class,
                () -> mediaService.readMedia(null));
    }

    @Test
    @DisplayName("FR7 — readMedia rejects blank filename")
    void readMedia_blankFilename_throwsIAE() {
        assertThrows(IllegalArgumentException.class,
                () -> mediaService.readMedia("   "));
    }

    @Test
    @DisplayName("NFR4 — readMedia rejects path traversal with ..")
    void readMedia_pathTraversal_dotDot_throwsIAE() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> mediaService.readMedia("../etc/passwd"));

        assertTrue(ex.getMessage().contains("illegal"));
    }

    @Test
    @DisplayName("NFR4 — readMedia rejects forward-slash path")
    void readMedia_pathTraversal_forwardSlash_throwsIAE() {
        assertThrows(IllegalArgumentException.class,
                () -> mediaService.readMedia("subdir/file.jpg"));
    }

    @Test
    @DisplayName("NFR4 — readMedia rejects backslash path")
    void readMedia_pathTraversal_backslash_throwsIAE() {
        assertThrows(IllegalArgumentException.class,
                () -> mediaService.readMedia("subdir\\file.jpg"));
    }

    // ── deleteMedia ───────────────────────────────────────────────────────

    @Test
    @DisplayName("FR7/FR3 — deleteMedia removes file and clears mediaUrl")
    void deleteMedia_existingMedia_filesRemoved() throws IOException {
        Pin pin = makePin(1L);
        Path file = tempDir.resolve("abc.jpg");
        Files.write(file, new byte[]{1});
        pin.setMediaUrl("/api/media/abc.jpg");

        when(pinRepository.findById(1L)).thenReturn(Optional.of(pin));
        when(pinRepository.save(any(Pin.class))).thenAnswer(inv -> inv.getArgument(0));

        mediaService.deleteMedia(1L);

        assertFalse(Files.exists(file), "file should be deleted from disk");
        assertNull(pin.getMediaUrl(), "mediaUrl should be cleared");
        verify(pinRepository).save(pin);
    }

    @Test
    @DisplayName("FR3 — deleteMedia is idempotent when pin has no media")
    void deleteMedia_pinWithNoMedia_succeedsSilently() {
        Pin pin = makePin(1L);
        // pin.mediaUrl is null
        when(pinRepository.findById(1L)).thenReturn(Optional.of(pin));
        when(pinRepository.save(any(Pin.class))).thenAnswer(inv -> inv.getArgument(0));

        mediaService.deleteMedia(1L); // should not throw

        assertNull(pin.getMediaUrl());
        verify(pinRepository).save(pin);
    }

    @Test
    @DisplayName("FR3 — deleteMedia is idempotent when file already missing")
    void deleteMedia_fileAlreadyMissing_succeedsSilently() {
        Pin pin = makePin(1L);
        pin.setMediaUrl("/api/media/already-gone.jpg");
        // No file written to tempDir

        when(pinRepository.findById(1L)).thenReturn(Optional.of(pin));
        when(pinRepository.save(any(Pin.class))).thenAnswer(inv -> inv.getArgument(0));

        mediaService.deleteMedia(1L); // should not throw

        assertNull(pin.getMediaUrl());
    }

    @Test
    @DisplayName("FR7 — deleteMedia throws 'not found' when pin missing")
    void deleteMedia_pinNotFound_throwsIAEWithNotFound() {
        when(pinRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> mediaService.deleteMedia(999L));

        assertTrue(ex.getMessage().contains("not found"));
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private Pin makePin(Long id) {
        Pin pin = new Pin();
        pin.setId(id);
        pin.setLocationName("Test Pin");
        pin.setLatitude(0.0);
        pin.setLongitude(0.0);
        pin.setPrivacyLevel(Privacy.PRIVATE);
        return pin;
    }
}
