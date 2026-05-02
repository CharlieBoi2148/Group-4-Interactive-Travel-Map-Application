package travelmap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import travelmap.controller.MediaController;
import travelmap.media.MediaService;
import travelmap.model.Pin;
import travelmap.model.Privacy;

/**
 * MediaControllerTest — JUnit 5 + MockMvc tests for FR7 Media REST endpoints.
 *
 * Test strategy:
 *   - @WebMvcTest(MediaController.class) — slice test, only the controller
 *     and its supporting infrastructure are loaded. No DB, no full Spring
 *     context.
 *   - MediaService is mocked with @MockBean so we can drive every code
 *     path (success, IAE 400, IAE 404, ISE 500) directly.
 *   - Tests focus on HTTP-layer behavior:
 *       - Status code mapping (201, 200, 204, 400, 404, 500)
 *       - Content-Type header on getMedia
 *       - Response body shape on uploadMedia (Pin JSON)
 *       - Multipart request parsing
 *
 * SRS coverage:
 *   FR7 — every endpoint exercised at the HTTP layer
 *   NFR3 — disk I/O failure mapping to HTTP 500 is verified
 */
@WebMvcTest(MediaController.class)
class MediaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MediaService mediaService;

    // ── uploadMedia ───────────────────────────────────────────────────────

    @Test
    @DisplayName("FR7 — POST upload returns 201 with updated Pin on success")
    void uploadMedia_success_returns201WithPin() throws Exception {
        Pin saved = makePin(1L);
        saved.setMediaUrl("/api/media/abc.jpg");
        when(mediaService.uploadMedia(anyLong(), any(MultipartFile.class)))
                .thenReturn(saved);

        MockMultipartFile file = new MockMultipartFile(
                "file", "vacation.jpg", "image/jpeg", new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/api/media/1").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.mediaUrl").value("/api/media/abc.jpg"));
    }

    @Test
    @DisplayName("FR7 — POST upload returns 400 when service rejects file")
    void uploadMedia_invalidFile_returns400() throws Exception {
        when(mediaService.uploadMedia(anyLong(), any(MultipartFile.class)))
                .thenThrow(new IllegalArgumentException("File type not allowed"));

        MockMultipartFile file = new MockMultipartFile(
                "file", "evil.txt", "text/plain", new byte[]{1});

        mockMvc.perform(multipart("/api/media/1").file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("FR7 — POST upload returns 404 when pin not found")
    void uploadMedia_pinNotFound_returns404() throws Exception {
        when(mediaService.uploadMedia(anyLong(), any(MultipartFile.class)))
                .thenThrow(new IllegalArgumentException("Pin not found: 999"));

        MockMultipartFile file = new MockMultipartFile(
                "file", "x.jpg", "image/jpeg", new byte[]{1});

        mockMvc.perform(multipart("/api/media/999").file(file))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("NFR3 — POST upload returns 500 on disk I/O failure")
    void uploadMedia_diskFailure_returns500() throws Exception {
        when(mediaService.uploadMedia(anyLong(), any(MultipartFile.class)))
                .thenThrow(new IllegalStateException("Failed to save media file"));

        MockMultipartFile file = new MockMultipartFile(
                "file", "x.jpg", "image/jpeg", new byte[]{1});

        mockMvc.perform(multipart("/api/media/1").file(file))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("FR7 — POST upload missing file param returns 400")
    void uploadMedia_missingFileParam_returns400() throws Exception {
        // No file attached — Spring's MultipartResolver should reject this
        // before reaching the controller method, returning 400.
        mockMvc.perform(multipart("/api/media/1"))
                .andExpect(status().isBadRequest());
    }

    // ── getMedia ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("FR7 — GET media returns 200 with bytes and image/jpeg Content-Type")
    void getMedia_existingJpg_returns200WithBytes() throws Exception {
        byte[] bytes = new byte[]{1, 2, 3, 4};
        when(mediaService.readMedia("test.jpg")).thenReturn(bytes);

        mockMvc.perform(get("/api/media/test.jpg"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/jpeg"))
                .andExpect(content().bytes(bytes));
    }

    @Test
    @DisplayName("FR7 — GET media falls back to octet-stream for unknown extension")
    void getMedia_unknownExtension_returnsOctetStream() throws Exception {
        byte[] bytes = new byte[]{1};
        when(mediaService.readMedia("file.xyz")).thenReturn(bytes);

        mockMvc.perform(get("/api/media/file.xyz"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type",
                        "application/octet-stream"));
    }

    @Test
    @DisplayName("FR7 — GET media returns 404 when file not found")
    void getMedia_notFound_returns404() throws Exception {
        when(mediaService.readMedia(anyString()))
                .thenThrow(new IllegalArgumentException(
                        "Media file not found: gone.jpg"));

        mockMvc.perform(get("/api/media/gone.jpg"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("NFR4 — GET media returns 400 on path traversal attempt")
    void getMedia_pathTraversal_returns400() throws Exception {
        when(mediaService.readMedia(anyString()))
                .thenThrow(new IllegalArgumentException(
                        "Filename contains illegal characters"));

        mockMvc.perform(get("/api/media/safe.jpg"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("NFR3 — GET media returns 500 on disk read failure")
    void getMedia_diskFailure_returns500() throws Exception {
        when(mediaService.readMedia(anyString()))
                .thenThrow(new IllegalStateException("Failed to read"));

        mockMvc.perform(get("/api/media/x.jpg"))
                .andExpect(status().isInternalServerError());
    }

    // ── deleteMedia ───────────────────────────────────────────────────────

    @Test
    @DisplayName("FR7/FR3 — DELETE media returns 204 on success")
    void deleteMedia_success_returns204() throws Exception {
        doNothing().when(mediaService).deleteMedia(1L);

        mockMvc.perform(delete("/api/media/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("FR3 — DELETE media is idempotent (204 even when no media existed)")
    void deleteMedia_noMedia_returns204() throws Exception {
        // Service treats the no-media case as a successful no-op
        doNothing().when(mediaService).deleteMedia(1L);

        mockMvc.perform(delete("/api/media/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("FR7 — DELETE media returns 404 when pin not found")
    void deleteMedia_pinNotFound_returns404() throws Exception {
        doThrow(new IllegalArgumentException("Pin not found: 999"))
                .when(mediaService).deleteMedia(999L);

        mockMvc.perform(delete("/api/media/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("NFR3 — DELETE media returns 500 on disk delete failure")
    void deleteMedia_diskFailure_returns500() throws Exception {
        doThrow(new IllegalStateException("Failed to delete"))
                .when(mediaService).deleteMedia(1L);

        mockMvc.perform(delete("/api/media/1"))
                .andExpect(status().isInternalServerError());
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
