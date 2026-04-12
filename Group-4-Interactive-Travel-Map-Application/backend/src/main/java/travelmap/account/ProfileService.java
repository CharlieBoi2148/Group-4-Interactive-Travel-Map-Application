package travelmap.account;

import travelmap.interfaces.*;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class ProfileService implements IProfileService {
    private UserRepository userRepository;
    
    @Autowired
    public ProfileService(UserRepository userRepository) {
    this.userRepository = userRepository;
}
    public void updateProfile(String userId, Object data) {
    userRepository.update(userId, data);
}
}
