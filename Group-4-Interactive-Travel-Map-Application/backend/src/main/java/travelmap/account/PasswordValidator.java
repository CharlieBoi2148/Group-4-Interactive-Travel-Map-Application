package travelmap.account;

import org.springframework.stereotype.Component;

@Component
public class PasswordValidator {
    public boolean validatePassword(String password) { return false; }
    public String hashPassword(String password) { return null; }
    public boolean checkMatch(String raw, String hashed) { return false; }
}
