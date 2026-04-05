package travelmap.interfaces;

public interface IPinController {
    void handleCreatePin(Object formData);
    void handleEditPin(String pinId, Object updates);
    void handleDeletePin(String pinId);
    void handleViewPins();
    void handleSetPinPrivacy(String pinId, String privacyLevel);
}
