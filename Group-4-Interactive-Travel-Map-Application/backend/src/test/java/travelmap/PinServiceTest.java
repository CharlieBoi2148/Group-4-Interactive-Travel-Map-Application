package travelmap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import travelmap.model.Pin;
import travelmap.model.Privacy;
import travelmap.repository.PinRepository;
import travelmap.repository.PinService;

/**
 * PinServiceTest — Unit tests for PinService (Model/Service layer).
 *
 * Tests the business logic in PinService in complete isolation from the
 * database. PinRepository is mocked using Mockito — no H2 database is
 * started, no Spring context is loaded. This makes tests fast and reliable.
 *
 * Testing philosophy (AAA pattern):
 *   Arrange — set up the data and mock behavior
 *   Act     — call the method being tested
 *   Assert  — verify the result
 *
 * Why mock PinRepository?
 *   PinService depends on PinRepository. If we used the real repository,
 *   we'd need a database connection and a Spring context — that's an
 *   integration test, not a unit test. By mocking PinRepository we test
 *   only PinService's logic, not Spring Data JPA's behavior.
 *
 * @ExtendWith(MockitoExtension.class) replaces the old @RunWith(MockitoJUnitRunner.class)
 * in JUnit 5. It initializes all @Mock fields automatically before each test.
 *
 * SRS coverage:
 *   FR1  — createPin() tests
 *   FR2  — updatePin() tests
 *   FR3  — deletePin() tests
 *   FR4  — getAllPins() tests
 *   FR11 — setPinPrivacy() tests
 */
@ExtendWith(MockitoExtension.class)
class PinServiceTest {

    @Mock
    private PinRepository pinRepository;

    private PinService pinService;

    /**
     * Runs before each test — creates a fresh PinService with the mock
     * repository injected via constructor. This is why constructor injection
     * was used in PinService — it makes testing straightforward.
     */
    @BeforeEach
    void setUp() {
        pinService = new PinService(pinRepository);
    }

    // ── Helper — builds a valid Pin for reuse across tests ───────────────────

    private Pin buildValidPin() {
        Pin pin = new Pin();
        pin.setLocationName("Eiffel Tower");
        pin.setCountry("France");
        pin.setRegion("Ile-de-France");
        pin.setVisitDate(LocalDate.of(2024, 6, 1));
        pin.setNotes("Amazing view");
        pin.setLatitude(48.8584);
        pin.setLongitude(2.2945);
        pin.setPrivacyLevel(Privacy.PRIVATE);
        return pin;
    }

    // ── FR1: createPin() ─────────────────────────────────────────────────────

    @Test
    void createPin_savesAndReturnsPin() {
        // Arrange
        Pin input = buildValidPin();
        Pin saved = buildValidPin();
        saved.setId(1L);
        when(pinRepository.save(any(Pin.class))).thenReturn(saved);

        // Act
        Pin result = pinService.createPin(input, "alice");

        // Assert
        assertNotNull(result);
        assertEquals("Eiffel Tower", result.getLocationName());
        assertEquals(1L, result.getId());
        verify(pinRepository, times(1)).save(input);
    }

    @Test
    void createPin_defaultsPrivacyToPrivate_whenNotSet() {
        // Arrange — pin with no privacy level set
        Pin input = buildValidPin();
        input.setPrivacyLevel(null);
        when(pinRepository.save(any(Pin.class))).thenReturn(input);

        // Act
        pinService.createPin(input, "alice");

        // Assert — privacy should be set to PRIVATE before saving
        assertEquals(Privacy.PRIVATE, input.getPrivacyLevel());
    }

    @Test
    void createPin_throwsException_whenLocationNameIsNull() {
        // Arrange
        Pin input = buildValidPin();
        input.setLocationName(null);

        // Act + Assert — no save should happen
        assertThrows(IllegalArgumentException.class, () -> pinService.createPin(input, "alice"));
        verify(pinRepository, never()).save(any());
    }

    @Test
    void createPin_throwsException_whenLocationNameIsBlank() {
        // Arrange
        Pin input = buildValidPin();
        input.setLocationName("   ");

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> pinService.createPin(input, "alice"));
        verify(pinRepository, never()).save(any());
    }

    @Test
    void createPin_throwsException_whenLatitudeIsNull() {
        // Arrange — missing coordinates is an FR1 validation failure
        Pin input = buildValidPin();
        input.setLatitude(null);

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> pinService.createPin(input, "alice"));
        verify(pinRepository, never()).save(any());
    }

    @Test
    void createPin_throwsException_whenLongitudeIsNull() {
        // Arrange
        Pin input = buildValidPin();
        input.setLongitude(null);

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> pinService.createPin(input, "alice"));
        verify(pinRepository, never()).save(any());
    }

    @Test
    void createPin_throwsException_whenOwnerIdIsNull() {
        // Arrange
        Pin input = buildValidPin();

        // Act + Assert — NFR4: ownerId is mandatory
        assertThrows(IllegalArgumentException.class, () -> pinService.createPin(input, null));
        verify(pinRepository, never()).save(any());
    }

    @Test
    void createPin_throwsException_whenOwnerIdIsBlank() {
        // Arrange
        Pin input = buildValidPin();

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> pinService.createPin(input, "   "));
        verify(pinRepository, never()).save(any());
    }

    // ── FR4: getAllPins() ─────────────────────────────────────────────────────

    @Test
    void getAllPins_returnsOwnersPins() {
        // Arrange
        Pin pin1 = buildValidPin();
        pin1.setId(1L);
        pin1.setOwnerId("alice");
        Pin pin2 = buildValidPin();
        pin2.setId(2L);
        pin2.setLocationName("Colosseum");
        pin2.setOwnerId("alice");
        when(pinRepository.findByOwnerId("alice")).thenReturn(List.of(pin1, pin2));

        // Act
        List<Pin> result = pinService.getAllPins("alice");

        // Assert
        assertEquals(2, result.size());
        verify(pinRepository, times(1)).findByOwnerId("alice");
    }

    @Test
    void getAllPins_returnsEmptyList_whenOwnerHasNoPins() {
        // Arrange
        when(pinRepository.findByOwnerId("alice")).thenReturn(List.of());

        // Act
        List<Pin> result = pinService.getAllPins("alice");

        // Assert — never null, always a list React can call .map() on
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllPins_doesNotReturnOtherUsersPins() {
        // Arrange — alice has one pin, bob's pin is not returned
        Pin alicePin = buildValidPin();
        alicePin.setId(1L);
        alicePin.setOwnerId("alice");
        when(pinRepository.findByOwnerId("alice")).thenReturn(List.of(alicePin));

        // Act
        List<Pin> result = pinService.getAllPins("alice");

        // Assert — only alice's pin returned; bob's pin never loaded
        assertEquals(1, result.size());
        assertEquals("alice", result.get(0).getOwnerId());
        verify(pinRepository, never()).findAll();
    }

    @Test
    void getAllPins_throwsException_whenOwnerIdIsNull() {
        assertThrows(IllegalArgumentException.class, () -> pinService.getAllPins(null));
        verify(pinRepository, never()).findByOwnerId(any());
    }

    @Test
    void getAllPins_throwsException_whenOwnerIdIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> pinService.getAllPins("   "));
        verify(pinRepository, never()).findByOwnerId(any());
    }

    // ── FR2: updatePin() ─────────────────────────────────────────────────────

    @Test
    void updatePin_updatesAndReturnsSavedPin() {
        // Arrange
        Pin existing = buildValidPin();
        existing.setId(1L);

        Pin updates = new Pin();
        updates.setLocationName("Updated Tower");
        updates.setNotes("New notes");

        when(pinRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(pinRepository.save(any(Pin.class))).thenReturn(existing);

        // Act
        Pin result = pinService.updatePin(1L, updates);

        // Assert — updated fields applied
        assertEquals("Updated Tower", result.getLocationName());
        assertEquals("New notes", result.getNotes());
        verify(pinRepository, times(1)).save(existing);
    }

    @Test
    void updatePin_doesNotOverwriteNullFields() {
        // Arrange — updates object has only locationName, notes is null
        Pin existing = buildValidPin();
        existing.setId(1L);
        existing.setNotes("Original notes");

        Pin updates = new Pin();
        updates.setLocationName("Updated Tower");
        // notes is null — should not overwrite existing notes

        when(pinRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(pinRepository.save(any(Pin.class))).thenReturn(existing);

        // Act
        pinService.updatePin(1L, updates);

        // Assert — original notes preserved
        assertEquals("Original notes", existing.getNotes());
    }

    @Test
    void updatePin_throwsException_whenPinNotFound() {
        // Arrange
        when(pinRepository.findById(99L)).thenReturn(Optional.empty());

        // Act + Assert — PinController catches this and returns HTTP 404
        assertThrows(IllegalArgumentException.class,
            () -> pinService.updatePin(99L, new Pin()));
        verify(pinRepository, never()).save(any());
    }

    // ── FR3: deletePin() ─────────────────────────────────────────────────────

    @Test
    void deletePin_deletesExistingPin() {
        // Arrange
        Pin existing = buildValidPin();
        existing.setId(1L);
        when(pinRepository.findById(1L)).thenReturn(Optional.of(existing));

        // Act
        pinService.deletePin(1L);

        // Assert — delete called exactly once with the correct pin object
        verify(pinRepository, times(1)).delete(existing);
    }

    @Test
    void deletePin_throwsException_whenPinNotFound() {
        // Arrange
        when(pinRepository.findById(99L)).thenReturn(Optional.empty());

        // Act + Assert — PinController catches this and returns HTTP 404
        assertThrows(IllegalArgumentException.class,
            () -> pinService.deletePin(99L));
        verify(pinRepository, never()).delete(any());
    }

    // ── FR11: setPinPrivacy() ─────────────────────────────────────────────────

    @Test
    void setPinPrivacy_updatesPrivacyLevel() {
        // Arrange
        Pin existing = buildValidPin();
        existing.setId(1L);
        existing.setPrivacyLevel(Privacy.PRIVATE);
        when(pinRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(pinRepository.save(any(Pin.class))).thenReturn(existing);

        // Act
        Pin result = pinService.setPinPrivacy(1L, Privacy.PUBLIC);

        // Assert — privacy updated immediately per FR11
        assertEquals(Privacy.PUBLIC, result.getPrivacyLevel());
        verify(pinRepository, times(1)).save(existing);
    }

    @Test
    void setPinPrivacy_throwsException_whenPinNotFound() {
        // Arrange
        when(pinRepository.findById(99L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(IllegalArgumentException.class,
            () -> pinService.setPinPrivacy(99L, Privacy.PUBLIC));
        verify(pinRepository, never()).save(any());
    }
}