package travelmap.account;

import travelmap.interfaces.*;
import travelmap.model.User;
import org.springframework.stereotype.Service;

@Service
public class AuthService implements IAuthService {
    private User currentUser;
    private SessionManager sessionManager;
    private PasswordValidator passwordValidator;
    private UserRepository userRepository;

    public boolean isLoggedIn() { return false; }
    public User getCurrentUser() { return null; }
    public boolean login(String username, String password) { return false; }
    public void logout() {}
}
