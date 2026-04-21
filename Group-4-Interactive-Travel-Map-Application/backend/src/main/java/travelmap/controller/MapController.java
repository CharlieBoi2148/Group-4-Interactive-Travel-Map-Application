package travelmap.controller;

import travelmap.interfaces.IMapController;
import travelmap.interfaces.MapService;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api/map")
public class MapController implements IMapController {
    private MapService mapService;
    public void handleVisualizeMap() {}
    public void handleCalculateDistance() {}
}
