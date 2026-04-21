package travelmap.interfaces;

import travelmap.model.User;

public interface IAuthService {
    boolean login(String username, String password);
    void logout();
    boolean isLoggedIn();
    User getCurrentUser();
}
