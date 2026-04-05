package travelmap.model;

import java.time.LocalDateTime;
import java.util.List;

public class Trip {
    private String tripId;
    private String name;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<Pin> pins;
    private Privacy privacyLevel;
    private String ownerId;
}
