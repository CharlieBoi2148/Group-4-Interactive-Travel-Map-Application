package travelmap.repository;

import travelmap.interfaces.UserRepository;
import travelmap.model.User;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;

@Repository
public class UserRepositoryImpl implements UserRepository {
    private List<User> users = new ArrayList<>();

    public User findByUsername(String username) {
        for (User user : users) {
            if (user.getUsername() != null && user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }

    public void save(User user) {
        users.add(user);
    }

    public void update(String userId, Object data) {
        if (!(data instanceof User)) {
            return;
        }

        User updatedData = (User) data;

        for (User user : users) {
            if (user.getUserId() != null && user.getUserId().equals(userId)) {
                if (updatedData.getUsername() != null) {
                    user.setUsername(updatedData.getUsername());
                }
                if (updatedData.getProfilePicture() != null) {
                    user.setProfilePicture(updatedData.getProfilePicture());
                }
                if (updatedData.getHomeLocation() != null) {
                    user.setHomeLocation(updatedData.getHomeLocation());
                }
                if (updatedData.getMeasurementPreference() != null) {
                    user.setMeasurementPreference(updatedData.getMeasurementPreference());
                }
                break;
            }
        }
    }
}
