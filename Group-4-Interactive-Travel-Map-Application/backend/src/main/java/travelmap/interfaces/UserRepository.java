package travelmap.interfaces;

import travelmap.model.User;

public interface UserRepository {
    User findByUsername(String username);
    void save(User user);
    void update(String userId, Object data);
}
