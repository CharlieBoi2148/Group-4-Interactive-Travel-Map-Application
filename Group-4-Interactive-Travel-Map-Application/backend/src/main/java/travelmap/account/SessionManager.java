package travelmap.account;

import org.springframework.stereotype.Component;

@Component
public class SessionManager {
    private String sessionToken;

    public String generateSessionToken() { 
        sessionToken = java.util.UUID.randomUUID().toString();
        return sessionToken;
    }
    public void invalidateSession() {
        sessionToken = null;
    }
    public String getToken() { 
        return sessionToken;
    }
    public boolean isTokenValid() { 
        return sessionToken != null && !sessionToken.isEmpty();
    }
}
