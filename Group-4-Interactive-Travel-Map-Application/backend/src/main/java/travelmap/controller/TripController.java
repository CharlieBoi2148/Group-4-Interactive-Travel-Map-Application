package travelmap.controller;

import travelmap.interfaces.ITripController;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api/trips")
public class TripController implements ITripController {
    public void handleOrganizeTrip(Object tripData) {}
    public void handleViewTimeline() {}
    public void handleSetTripPrivacy(String tripId, String privacyLevel) {}
}
