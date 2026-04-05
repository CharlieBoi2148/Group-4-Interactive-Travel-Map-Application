package travelmap.interfaces;

import travelmap.model.User;

public interface IAuthService {
    boolean isLoggedIn();
    User getCurrentUser();
}
