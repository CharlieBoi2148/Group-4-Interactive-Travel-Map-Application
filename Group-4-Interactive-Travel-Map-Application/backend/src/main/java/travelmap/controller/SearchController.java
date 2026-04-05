package travelmap.controller;

import travelmap.interfaces.ISearchController;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api/search")
public class SearchController implements ISearchController {
    public void handleFilterSearch(Object criteria) {}
    public void handleViewStatistics() {}
}
