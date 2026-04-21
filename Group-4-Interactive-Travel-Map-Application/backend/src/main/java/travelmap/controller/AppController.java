package travelmap.controller;

import travelmap.account.AccountFacade;
import travelmap.interfaces.*;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AppController {
    private AccountFacade accountFacade;
    private IPinController pinController;
    private ITripController tripController;
    private IMediaController mediaController;
    private IMapController mapController;
    private ISearchController searchController;
    private ISharingController sharingController;
    private IAuthService authService;
}
