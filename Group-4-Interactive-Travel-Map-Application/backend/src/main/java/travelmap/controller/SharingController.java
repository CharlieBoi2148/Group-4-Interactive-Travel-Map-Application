package travelmap.controller;

import travelmap.interfaces.ISharingController;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api/share")
public class SharingController implements ISharingController {
    public void handleGenerateShareLink(String contentId) {}
}
