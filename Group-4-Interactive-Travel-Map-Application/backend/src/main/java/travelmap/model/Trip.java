package travelmap.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;

/**
 * Trip — Model layer (MVC).
 *
 * Represents a user-created trip that groups pins and timeline metadata.
 * This class is a JPA entity and is designed for persistence through a future
 * TripRepository/TripService implementation.
 *
 * <p>{@code pins} is {@code @Transient} until {@code Pin} is a JPA entity and a
 * proper {@code tripId} / {@code @ManyToOne} mapping is merged — otherwise Hibernate
 * cannot persist {@code Trip}.
 */
@Entity
public class Trip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;

    @Transient
    private List<Pin> pins;

    @Enumerated(EnumType.STRING)
    private Privacy privacyLevel;

    private String ownerId;

    /**
     * Default constructor required by JPA.
     */
    public Trip() {
        this.pins = new ArrayList<>();
        this.privacyLevel = Privacy.PRIVATE;
    }

    /**
     * Convenience constructor for creating a trip in application code.
     *
     * @param name trip name
     * @param description trip description
     * @param startDate trip start date
     * @param endDate trip end date
     * @param pins associated pins
     * @param privacyLevel trip visibility level
     * @param ownerId owner user identifier
     */
    public Trip(
            String name,
            String description,
            LocalDate startDate,
            LocalDate endDate,
            List<Pin> pins,
            Privacy privacyLevel,
            String ownerId) {
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.pins = pins != null ? pins : new ArrayList<>();
        this.privacyLevel = privacyLevel != null ? privacyLevel : Privacy.PRIVATE;
        this.ownerId = ownerId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public List<Pin> getPins() {
        return pins;
    }

    public void setPins(List<Pin> pins) {
        this.pins = pins;
    }

    public Privacy getPrivacyLevel() {
        return privacyLevel;
    }

    public void setPrivacyLevel(Privacy privacyLevel) {
        this.privacyLevel = privacyLevel;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }
}
