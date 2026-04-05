package travelmap.interfaces;

public interface ITripController {
    void handleOrganizeTrip(Object tripData);
    void handleViewTimeline();
    void handleSetTripPrivacy(String tripId, String privacyLevel);
}
