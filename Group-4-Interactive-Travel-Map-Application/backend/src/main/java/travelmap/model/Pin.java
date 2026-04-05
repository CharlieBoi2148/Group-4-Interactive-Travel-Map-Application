package travelmap.model;

import java.time.LocalDateTime;
import java.util.List;

public class Pin {
    private String pinId;
    private String locationName;
    private Coordinate coordinates;
    private String country;
    private String region;
    private LocalDateTime visitDate;
    private String notes;
    private Privacy privacyLevel;
    private List<Media> media;
}
