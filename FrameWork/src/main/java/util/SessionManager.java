package util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SessionManager {
    private static SessionManager instance;
    private Map<String, Session> sessions;
    private long sessionTimeoutMillis;
    
    private SessionManager() {
        this.sessions = new HashMap<>();
        this.sessionTimeoutMillis = 30 * 60 * 1000; // 30 minutes par défaut
    }
    
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    public Session getSession(String sessionId) {
        if (sessionId == null) {
            return createSession();
        }
        
        Session session = sessions.get(sessionId);
        if (session == null || session.isExpired(sessionTimeoutMillis)) {
            sessions.remove(sessionId);
            return createSession();
        }
        
        return session;
    }
    
    public Session createSession() {
        String sessionId = UUID.randomUUID().toString();
        Session session = new Session(sessionId);
        sessions.put(sessionId, session);
        return session;
    }
    
    public void invalidate(String sessionId) {
        Session session = sessions.get(sessionId);
        if (session != null) {
            session.invalidate();
            sessions.remove(sessionId);
        }
    }
    
    public void setSessionTimeout(long timeoutMillis) {
        this.sessionTimeoutMillis = timeoutMillis;
    }
    
    public void cleanupExpiredSessions() {
        sessions.entrySet().removeIf(entry -> 
            entry.getValue().isExpired(sessionTimeoutMillis)
        );
    }
}
