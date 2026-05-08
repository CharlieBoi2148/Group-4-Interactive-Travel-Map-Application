package travelmap.repository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import travelmap.model.Privacy;
import travelmap.model.Trip;

/**
 * Service layer for trip business logic.
 *
 * Note: This class is currently placed in the repository package to match the
 * project's existing package structure. It can be moved to travelmap.service
 * during the planned package cleanup.
 */
@Service
public class TripService {
    private final TripRepository tripRepository;

    /**
     * Construct a TripService with repository dependency.
     *
     * @param tripRepository repository used for trip persistence
     */
    public TripService(TripRepository tripRepository) {
        this.tripRepository = tripRepository;
    }

    /**
     * Create and persist a new trip.
     *
     * @param trip trip payload
     * @return persisted trip
     */
    public Trip createTrip(Trip trip) {
        if (trip.getName() == null || trip.getName().isBlank()) {
            throw new IllegalArgumentException("Trip name is required");
        }
        if (trip.getPrivacyLevel() == null) {
            trip.setPrivacyLevel(Privacy.PRIVATE);
        }
        return tripRepository.save(trip);
    }

    /**
     * Retrieve all trips.
     *
     * @return all trips
     */
    public List<Trip> getAllTrips() {
        return tripRepository.findAll();
    }

    public List<Trip> getTripsForOwner(String ownerId) {
        if (ownerId == null || ownerId.isBlank()) {
            return List.of();
        }
        return tripRepository.findByOwnerId(ownerId);
    }

    /**
     * Retrieve a trip by id.
     *
     * @param id trip id
     * @return optional trip
     */
    public Optional<Trip> getTripById(Long id) {
        return tripRepository.findById(id);
    }

    /**
     * Update mutable trip fields for an existing trip.
     *
     * @param id trip id
     * @param updates fields to update
     * @return optional updated trip
     */
    public Optional<Trip> updateTrip(Long id, Trip updates) {
        return tripRepository.findById(id).map(existing -> {
            if (updates.getName() != null) {
                if (updates.getName().isBlank()) {
                    throw new IllegalArgumentException("Trip name is required");
                }
                existing.setName(updates.getName());
            }
            if (updates.getDescription() != null) {
                existing.setDescription(updates.getDescription());
            }
            if (updates.getStartDate() != null) {
                existing.setStartDate(updates.getStartDate());
            }
            if (updates.getEndDate() != null) {
                existing.setEndDate(updates.getEndDate());
            }
            if (updates.getPrivacyLevel() != null) {
                existing.setPrivacyLevel(updates.getPrivacyLevel());
            }
            return tripRepository.save(existing);
        });
    }

    public Optional<Trip> updateTripForOwner(Long id, Trip updates, String ownerId) {
        return tripRepository.findById(id)
                .filter(trip -> ownerId != null && ownerId.equals(trip.getOwnerId()))
                .flatMap(trip -> updateTrip(id, updates));
    }

    /**
     * Retrieve timeline-ordered trips for an owner.
     *
     * @param ownerId owner identifier
     * @return trips sorted by start date then end date
     */
    public List<Trip> getTimelineForOwner(String ownerId) {
        List<Trip> trips = tripRepository.findByOwnerId(ownerId);
        trips.sort(
                Comparator.comparing(Trip::getStartDate, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Trip::getEndDate, Comparator.nullsLast(Comparator.naturalOrder())));
        return trips;
    }

    /**
     * Set privacy level for an existing trip.
     *
     * @param id trip id
     * @param privacyLevel privacy value
     * @return updated trip
     */
    public Optional<Trip> setTripPrivacy(Long id, Privacy privacyLevel) {
        return tripRepository.findById(id).map(trip -> {
            trip.setPrivacyLevel(privacyLevel);
            return tripRepository.save(trip);
        });
    }

    public Optional<Trip> setTripPrivacyForOwner(Long id, Privacy privacyLevel, String ownerId) {
        return tripRepository.findById(id)
                .filter(trip -> ownerId != null && ownerId.equals(trip.getOwnerId()))
                .flatMap(trip -> setTripPrivacy(id, privacyLevel));
    }

    /**
     * Delete a trip by id.
     *
     * @param id trip id
     */
    public void deleteTrip(Long id) {
        tripRepository.deleteById(id);
    }
}
