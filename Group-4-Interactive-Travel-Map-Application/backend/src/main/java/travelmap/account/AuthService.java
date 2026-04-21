package travelmap.account;

import travelmap.interfaces.*;
import travelmap.model.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService implements IAuthService {
    private User currentUser;
    private SessionManager sessionManager;
    private PasswordValidator passwordValidator;
    private UserRepository userRepository;

    @Autowired
    public AuthService(SessionManager sessionManager,
                       PasswordValidator passwordValidator,
                       UserRepository userRepository) {
        this.sessionManager = sessionManager;
        this.passwordValidator = passwordValidator;
        this.userRepository = userRepository;
    }

    public boolean isLoggedIn() { 
        return currentUser != null && sessionManager.isTokenValid();
    }
    
    public User getCurrentUser() { 
        return currentUser;
    }
    public boolean login(String username, String password) { 
        User user = userRepository.findByUsername(username);

    if (user == null) {
        return false;
    }

    if (!passwordValidator.checkMatch(password, user.getPassword())) {
        return false;
    }

    currentUser = user;
    sessionManager.generateSessionToken();
    return true;
    }
    public void logout() {
    currentUser = null;
    sessionManager.invalidateSession();
    }
}
