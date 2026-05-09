package travelmap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
import travelmap.model.Trip;
import travelmap.repository.PinRepository;
import travelmap.repository.PinService;
import travelmap.repository.TripRepository;

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

    @Mock
    private TripRepository tripRepository;

    private PinService pinService;

    /**
     * Runs before each test — creates a fresh PinService with the mock
     * repository injected via constructor. This is why constructor injection
     * was used in PinService — it makes testing straightforward.
     */
    @BeforeEach
    void setUp() {
        pinService = new PinService(pinRepository, tripRepository);
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
        Pin result = pinService.createPin(input);

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
        pinService.createPin(input);

        // Assert — privacy should be set to PRIVATE before saving
        assertEquals(Privacy.PRIVATE, input.getPrivacyLevel());
    }

    @Test
    void createPin_throwsException_whenLocationNameIsNull() {
        // Arrange
        Pin input = buildValidPin();
        input.setLocationName(null);

        // Act + Assert — no save should happen
        assertThrows(IllegalArgumentException.class, () -> pinService.createPin(input));
        verify(pinRepository, never()).save(any());
    }

    @Test
    void createPin_throwsException_whenLocationNameIsBlank() {
        // Arrange
        Pin input = buildValidPin();
        input.setLocationName("   ");

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> pinService.createPin(input));
        verify(pinRepository, never()).save(any());
    }

    @Test
    void createPin_throwsException_whenLatitudeIsNull() {
        // Arrange — missing coordinates is an FR1 validation failure
        Pin input = buildValidPin();
        input.setLatitude(null);

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> pinService.createPin(input));
        verify(pinRepository, never()).save(any());
    }

    @Test
    void createPin_throwsException_whenLongitudeIsNull() {
        // Arrange
        Pin input = buildValidPin();
        input.setLongitude(null);

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> pinService.createPin(input));
        verify(pinRepository, never()).save(any());
    }

    // ── FR4: getAllPins() ─────────────────────────────────────────────────────

    @Test
    void getAllPins_returnsAllPins() {
        // Arrange
        Pin pin1 = buildValidPin();
        pin1.setId(1L);
        Pin pin2 = buildValidPin();
        pin2.setId(2L);
        pin2.setLocationName("Colosseum");
        when(pinRepository.findAll()).thenReturn(List.of(pin1, pin2));

        // Act
        List<Pin> result = pinService.getAllPins();

        // Assert
        assertEquals(2, result.size());
        verify(pinRepository, times(1)).findAll();
    }

    @Test
    void getAllPins_returnsEmptyList_whenNoPinsExist() {
        // Arrange
        when(pinRepository.findAll()).thenReturn(List.of());

        // Act
        List<Pin> result = pinService.getAllPins();

        // Assert — never null, always a list React can call .map() on
        assertNotNull(result);
        assertTrue(result.isEmpty());
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
        verify(tripRepository, never()).findById(anyLong());
    }

    @Test
    void updatePin_clearsTrip_whenTripIdIsNoTripSentinel() {
        Pin existing = buildValidPin();
        existing.setId(1L);
        existing.setTripId(5L);

        Pin updates = new Pin();
        updates.setTripId(Pin.NO_TRIP_ASSIGNMENT);

        when(pinRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(pinRepository.save(any(Pin.class))).thenReturn(existing);

        pinService.updatePin(1L, updates);

        assertNull(existing.getTripId());
        verify(tripRepository, never()).findById(anyLong());
    }

    @Test
    void updatePin_assignsTrip_whenTripExistsAndOwnerMatches() {
        Pin existing = buildValidPin();
        existing.setId(1L);
        existing.setOwnerId("alice");

        Trip trip = new Trip();
        trip.setId(10L);
        trip.setOwnerId("alice");

        Pin updates = new Pin();
        updates.setTripId(10L);

        when(pinRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
        when(pinRepository.save(any(Pin.class))).thenReturn(existing);

        pinService.updatePin(1L, updates);

        assertEquals(10L, existing.getTripId());
    }

    @Test
    void updatePin_throwsWhenTripOwnerDoesNotMatchPin() {
        Pin existing = buildValidPin();
        existing.setId(1L);
        existing.setOwnerId("alice");

        Trip trip = new Trip();
        trip.setId(10L);
        trip.setOwnerId("bob");

        Pin updates = new Pin();
        updates.setTripId(10L);

        when(pinRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));

        assertThrows(IllegalArgumentException.class, () -> pinService.updatePin(1L, updates));
        verify(pinRepository, never()).save(any());
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