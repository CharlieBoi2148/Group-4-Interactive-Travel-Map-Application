package travelmap.account;

import org.springframework.stereotype.Component;

@Component
public class SessionManager {
    private String sessionToken;
    public String generateSessionToken() { return null; }
    public void invalidateSession() {}
    public String getToken() { return sessionToken; }
    public boolean isTokenValid() { return false; }
}
