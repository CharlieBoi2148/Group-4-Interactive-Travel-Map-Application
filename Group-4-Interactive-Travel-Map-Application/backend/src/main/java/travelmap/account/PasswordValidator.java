package travelmap.account;

import org.springframework.stereotype.Component;

@Component
public class PasswordValidator {
    public boolean validatePassword(String password) { 
        if (password == null){ 
            return false;
        }
        return password.length() >= 8; 
    }
    public String hashPassword(String password) { 
        if (password == null) {
        return null;
    }
    return Integer.toHexString(password.hashCode());
    }
    public boolean checkMatch(String raw, String hashed) {
    if (raw == null || hashed == null) {
        return false;
    }
    return hashPassword(raw).equals(hashed);
    }
}
