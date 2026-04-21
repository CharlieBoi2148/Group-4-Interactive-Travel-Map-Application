package travelmap.controller;

import travelmap.interfaces.IMediaController;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api/media")
public class MediaController implements IMediaController {
    public void handleUploadMedia(Object file, String pinId) {}
}
