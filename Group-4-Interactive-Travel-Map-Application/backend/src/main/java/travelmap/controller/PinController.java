package travelmap.controller;

import travelmap.interfaces.IPinController;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api/pins")
public class PinController implements IPinController {
    public void handleCreatePin(Object formData) {}
    public void handleEditPin(String pinId, Object updates) {}
    public void handleDeletePin(String pinId) {}
    public void handleViewPins() {}
    public void handleSetPinPrivacy(String pinId, String privacyLevel) {}
}
