package travelmap.interfaces;

import travelmap.model.Pin;
import java.util.List;

public interface MapService {
    void renderMap(List<Pin> pins, List<Object> routes);
    float calculateDistance(Pin pin1, Pin pin2);
}
