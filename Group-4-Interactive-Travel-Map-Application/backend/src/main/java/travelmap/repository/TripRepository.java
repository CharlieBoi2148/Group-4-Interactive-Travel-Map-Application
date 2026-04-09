package travelmap.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import travelmap.model.Privacy;
import travelmap.model.Trip;

/**
 * Repository for persisting and querying {@link Trip} entities.
 */
@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {

    /**
     * Find all trips that belong to a specific owner.
     *
     * @param ownerId owner identifier
     * @return list of trips for the owner
     */
    List<Trip> findByOwnerId(String ownerId);

    /**
     * Find all trips with a specific privacy level.
     *
     * @param privacyLevel privacy value
     * @return list of matching trips
     */
    List<Trip> findByPrivacyLevel(Privacy privacyLevel);
}
