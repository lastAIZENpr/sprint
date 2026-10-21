# Guide du Sprint 9 : Gestion de Session

Ce document explique comment mettre en place une gestion de session personnalisée qui évite d'utiliser directement HttpSession.

## Objectifs
- Créer une classe Session personnalisée
- La session doit être disponible dans les méthodes d'action des contrôleurs
- Injection automatique de la session dans les méthodes (créer, modifier, supprimer)
- Éviter les problèmes de mémoire
- Une instance de session par client
- Stocker les sessions dans un Map dans le contexte (ServletContext)

## 1. Création de la classe `Session`

La classe Session encapsulera les données de session de manière indépendante de HttpSession.

```java
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
```

## 2. Création du `SessionManager`

Le SessionManager gère les sessions par client et les stocke dans le contexte.

```java
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
```

## 3. Modification de `ProcessRequest`

Ajouter la gestion de session dans ProcessRequest pour créer/récupérer les sessions.

## 4. Modification de `MethodExecutor`

Modifier MethodExecutor pour injecter automatiquement la session dans les méthodes qui ont un paramètre de type Session.

## 5. Exemple d'utilisation

```java
@Controller
public class UserController {
    
    @PostMapping(url = "/login")
    public String login(Session session, String username) {
        session.setAttribute("user", username);
        return "Utilisateur connecté: " + username;
    }
    
    @GetMapping(url = "/profile")
    public String profile(Session session) {
        String user = (String) session.getAttribute("user");
        return "Profil de: " + user;
    }
    
    @PostMapping(url = "/logout")
    public String logout(Session session) {
        session.invalidate();
        return "Déconnexion réussie";
    }
}
```
