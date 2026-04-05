package travelmap.repository;

import travelmap.interfaces.UserRepository;
import travelmap.model.User;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryImpl implements UserRepository {
    public User findByUsername(String username) { return null; }
    public void save(User user) {}
    public void update(String userId, Object data) {}
}
