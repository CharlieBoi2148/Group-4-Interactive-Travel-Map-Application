package travelmap.map;

import travelmap.interfaces.MapService;
import travelmap.model.Pin;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class MapAPIClient implements MapService {
    public void renderMap(List<Pin> pins, List<Object> routes) {}
    public float calculateDistance(Pin pin1, Pin pin2) { return 0.0f; }
}
