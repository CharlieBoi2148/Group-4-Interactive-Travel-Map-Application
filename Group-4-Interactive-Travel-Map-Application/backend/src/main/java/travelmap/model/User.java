package travelmap.model;

import java.util.List;

public class User {
    private String userId;
    private String username;
    private String password;
    private String profilePicture;
    private String homeLocation;
    private String measurementPreference;
    private List<Trip> trips;
    private Statistics statistics;
}
