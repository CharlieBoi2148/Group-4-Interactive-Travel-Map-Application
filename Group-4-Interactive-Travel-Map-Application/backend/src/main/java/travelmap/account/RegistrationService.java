package travelmap.account;

import travelmap.interfaces.*;
import travelmap.model.User;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class RegistrationService implements IRegistrationService {
    private UserRepository userRepository;
    private PasswordValidator passwordValidator;

@Autowired
public RegistrationService(UserRepository userRepository,
                           PasswordValidator passwordValidator) {
    this.userRepository = userRepository;
    this.passwordValidator = passwordValidator;
}
    public User register(Object userDetails) { 
        if (!(userDetails instanceof User)) {
        return null;
    }

    User user = (User) userDetails;

    if (user.getUsername() == null || user.getPassword() == null) {
        return null;
    }

    if (userRepository.findByUsername(user.getUsername()) != null) {
        return null;
    }

    if (!passwordValidator.validatePassword(user.getPassword())) {
        return null;
    }

    user.setPassword(passwordValidator.hashPassword(user.getPassword()));
    userRepository.save(user);

    return user;
    }
}
