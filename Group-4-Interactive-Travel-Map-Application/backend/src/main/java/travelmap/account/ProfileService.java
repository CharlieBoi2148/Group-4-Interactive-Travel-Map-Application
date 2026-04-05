package travelmap.account;

import travelmap.interfaces.*;
import org.springframework.stereotype.Service;

@Service
public class ProfileService implements IProfileService {
    private UserRepository userRepository;
    public void updateProfile(String userId, Object data) {}
}
