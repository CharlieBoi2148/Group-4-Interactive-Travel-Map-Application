package travelmap.account;

import travelmap.interfaces.*;
import travelmap.model.User;
import org.springframework.stereotype.Service;

@Service
public class RegistrationService implements IRegistrationService {
    private UserRepository userRepository;
    private PasswordValidator passwordValidator;
    public User register(Object userDetails) { return null; }
}
