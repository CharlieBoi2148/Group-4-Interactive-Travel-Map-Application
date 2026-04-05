package travelmap.account;

import travelmap.interfaces.*;
import travelmap.model.User;
import org.springframework.stereotype.Component;

@Component
public class AccountFacade {
    private IAuthService authService;
    private IRegistrationService registrationService;
    private IProfileService profileService;

    public boolean login(String username, String password) { return false; }
    public void logout() {}
    public boolean isLoggedIn() { return false; }
    public User getCurrentUser() { return null; }
    public User register(Object userDetails) { return null; }
    public void updateProfile(String userId, Object data) {}
}
