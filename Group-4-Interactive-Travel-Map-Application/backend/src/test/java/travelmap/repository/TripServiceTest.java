package travelmap.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import travelmap.model.Privacy;
import travelmap.model.Trip;

@ExtendWith(MockitoExtension.class)
class TripServiceTest {

    @Mock
    private TripRepository tripRepository;

    @InjectMocks
    private TripService tripService;

    private Trip validTrip;

    @BeforeEach
    void setUp() {
        validTrip = new Trip();
        validTrip.setName("Spring break");
        validTrip.setDescription("Beach");
    }

    @Test
    void createTrip_nullName_throws() {
        validTrip.setName(null);
        assertThrows(IllegalArgumentException.class, () -> tripService.createTrip(validTrip));
        verify(tripRepository, never()).save(any());
    }

    @Test
    void createTrip_blankName_throws() {
        validTrip.setName("   ");
        assertThrows(IllegalArgumentException.class, () -> tripService.createTrip(validTrip));
        verify(tripRepository, never()).save(any());
    }

    @Test
    void createTrip_defaultsPrivacyToPrivateAndSaves() {
        validTrip.setPrivacyLevel(null);
        when(tripRepository.save(validTrip)).thenAnswer(inv -> inv.getArgument(0));

        Trip saved = tripService.createTrip(validTrip);

        assertEquals(Privacy.PRIVATE, saved.getPrivacyLevel());
        verify(tripRepository).save(validTrip);
    }

    @Test
    void createTrip_preservesExplicitPrivacy() {
        validTrip.setPrivacyLevel(Privacy.PUBLIC);
        when(tripRepository.save(validTrip)).thenAnswer(inv -> inv.getArgument(0));

        Trip saved = tripService.createTrip(validTrip);

        assertEquals(Privacy.PUBLIC, saved.getPrivacyLevel());
        verify(tripRepository).save(validTrip);
    }

    @Test
    void getAllTrips_delegatesToRepository() {
        List<Trip> expected = List.of(validTrip);
        when(tripRepository.findAll()).thenReturn(expected);

        assertEquals(expected, tripService.getAllTrips());
        verify(tripRepository).findAll();
    }

    @Test
    void getTripById_returnsOptionalFromRepository() {
        when(tripRepository.findById(7L)).thenReturn(Optional.of(validTrip));
        assertEquals(Optional.of(validTrip), tripService.getTripById(7L));
    }

    @Test
    void getTripById_emptyWhenMissing() {
        when(tripRepository.findById(99L)).thenReturn(Optional.empty());
        assertTrue(tripService.getTripById(99L).isEmpty());
    }

    @Test
    void getTimelineForOwner_sortsByStartDateThenEndDate() {
        Trip later = new Trip("Later", null, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 10), null, null, "u1");
        Trip earlier = new Trip("Earlier", null, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 5), null, null, "u1");
        Trip sameStart = new Trip("SameStart", null, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 20), null, null, "u1");
        List<Trip> unsorted = new ArrayList<>(List.of(later, sameStart, earlier));
        when(tripRepository.findByOwnerId("u1")).thenReturn(unsorted);

        List<Trip> timeline = tripService.getTimelineForOwner("u1");

        assertEquals("Earlier", timeline.get(0).getName());
        assertEquals("SameStart", timeline.get(1).getName());
        assertEquals("Later", timeline.get(2).getName());
    }

    @Test
    void setTripPrivacy_updatesAndSavesWhenFound() {
        when(tripRepository.findById(3L)).thenReturn(Optional.of(validTrip));
        when(tripRepository.save(validTrip)).thenReturn(validTrip);

        Optional<Trip> result = tripService.setTripPrivacy(3L, Privacy.FRIENDS_ONLY);

        assertTrue(result.isPresent());
        assertEquals(Privacy.FRIENDS_ONLY, validTrip.getPrivacyLevel());
        verify(tripRepository).save(validTrip);
    }

    @Test
    void setTripPrivacy_emptyWhenNotFound() {
        when(tripRepository.findById(3L)).thenReturn(Optional.empty());

        assertTrue(tripService.setTripPrivacy(3L, Privacy.PUBLIC).isEmpty());
        verify(tripRepository, never()).save(any());
    }

    @Test
    void deleteTrip_delegatesToRepository() {
        tripService.deleteTrip(42L);
        verify(tripRepository).deleteById(42L);
    }
}
