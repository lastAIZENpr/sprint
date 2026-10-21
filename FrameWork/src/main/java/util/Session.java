package util;

import java.util.HashMap;
import java.util.Map;

public class Session {
    private String sessionId;
    private Map<String, Object> attributes;
    private long lastAccessedTime;
    private long creationTime;
    
    public Session(String sessionId) {
        this.sessionId = sessionId;
        this.attributes = new HashMap<>();
        this.creationTime = System.currentTimeMillis();
        this.lastAccessedTime = System.currentTimeMillis();
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
        lastAccessedTime = System.currentTimeMillis();
    }
    
    public Object getAttribute(String name) {
        lastAccessedTime = System.currentTimeMillis();
        return attributes.get(name);
    }
    
    public void removeAttribute(String name) {
        attributes.remove(name);
        lastAccessedTime = System.currentTimeMillis();
    }
    
    public void invalidate() {
        attributes.clear();
    }
    
    public long getLastAccessedTime() {
        return lastAccessedTime;
    }
    
    public long getCreationTime() {
        return creationTime;
    }
    
    public boolean isExpired(long timeoutMillis) {
        return (System.currentTimeMillis() - lastAccessedTime) > timeoutMillis;
    }
}
