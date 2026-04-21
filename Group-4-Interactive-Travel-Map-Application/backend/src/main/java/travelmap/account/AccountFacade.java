package travelmap.account;

import travelmap.interfaces.*;
import travelmap.model.User;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class AccountFacade {
    private IAuthService authService;
    private IRegistrationService registrationService;
    private IProfileService profileService;

    @Autowired
    public AccountFacade(IAuthService authService,
                         IRegistrationService registrationService,
                         IProfileService profileService) {
        this.authService = authService;
        this.registrationService = registrationService;
        this.profileService = profileService;
    }

    public boolean login(String username, String password) {
        return authService.login(username, password);
     }
    public void logout() {
        authService.logout();
    }
    public boolean isLoggedIn() {
        return authService.isLoggedIn();
     }
    public User getCurrentUser() {
        return authService.getCurrentUser(); 
    }
    public User register(Object userDetails) {
        return registrationService.register(userDetails);
     }
    public void updateProfile(String userId, Object data) {
        profileService.updateProfile(userId, data);
    }
}
