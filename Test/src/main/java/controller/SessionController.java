package main.java.controller;

import annotation.*;
import util.Session;

@Controller
@WebApi
public class SessionController {

    @PostMapping(url = "/login")
    public String login(Session session, String username) {
        session.setAttribute("user", username);
        return "Utilisateur connecté: " + username;
    }
    
    @GetMapping(url = "/profile")
    public String profile(Session session) {
        String user = (String) session.getAttribute("user");
        if (user == null) {
            return "Aucun utilisateur connecté";
        }
        return "Profil de: " + user;
    }
    
    @PostMapping(url = "/logout")
    public String logout(Session session) {
        session.invalidate();
        return "Déconnexion réussie";
    }
    
    @PostMapping(url = "/setdata")
    public String setData(Session session, String key, String value) {
        session.setAttribute(key, value);
        return "Donnée stockée: " + key + " = " + value;
    }
    
    @GetMapping(url = "/getdata")
    public String getData(Session session, String key) {
        Object value = session.getAttribute(key);
        if (value == null) {
            return "Aucune donnée pour la clé: " + key;
        }
        return "Donnée pour " + key + ": " + value;
    }
}
