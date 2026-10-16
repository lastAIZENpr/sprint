# Guide du Sprint 7 : Binding des données (Vue → Contrôleur)

Ce document explique comment mettre en place la récupération automatique des données (paramètres de formulaire et fichiers uploadés) depuis la vue pour les injecter dans les méthodes des contrôleurs. Il détaille la procédure et explique le **pourquoi** de chaque changement.

## Nouveauté : Réception complète d'objet
Au lieu de récupérer les paramètres un par un (`String nom, String prenom`), le framework est désormais capable d'instancier un objet complexe (ex: `User user`) et de le remplir automatiquement avec les données de la requête HTTP, y compris les fichiers uploadés !

## 1. Création de la classe `FileUpload`

**Pourquoi cette modification ?** 
Pour garder le contrôleur indépendant de l'API web (principe MVC), on crée notre propre modèle qui encapsule les données d'un fichier.

Créer un fichier `framework/src/main/java/rindra/framework/model/FileUpload.java` :

```java
package rindra.framework.model;

public class FileUpload {
    private String name;
    private byte[] content;
    
    // Getters, Setters, et Constructeurs
}
```

## 2. Modification de `FrontControllerServlet`

Le dispatcher a été refactorisé pour supporter la création dynamique d'objets. 

### A. Ajouter l'annotation `@MultipartConfig`

**Pourquoi cette modification ?**
Permet au serveur (Tomcat) de lire les requêtes `multipart/form-data`. Sans cela, la lecture des fichiers échoue.

### B. Modifier la façon dont on obtient la méthode

On itère désormais sur `getDeclaredMethods()` au lieu de faire `getDeclaredMethod("nom")` pour pouvoir trouver une méthode même si on ne connait pas encore le type exact de ses paramètres.

### C. Gérer le binding complet des paramètres

Voici la nouvelle logique implémentée dans la méthode `processRequest` :

```java
// ----- BINDING DES ARGUMENTS (COMPLET) -----
Parameter[] methodParameters = targetMethod.getParameters();
Object[] methodArgs = new Object[methodParameters.length];
boolean isMultipart = request.getContentType() != null && request.getContentType().startsWith("multipart/form-data");

for (int i = 0; i < methodParameters.length; i++) {
    Parameter param = methodParameters[i];
    String paramName = param.getName(); 
    Class<?> paramType = param.getType();

    if (paramType.equals(FileUpload.class)) {
        methodArgs[i] = extractFileUpload(paramName, request, isMultipart);
    } 
    else if (isSimpleType(paramType)) {
        methodArgs[i] = extractSimpleValue(paramName, paramType, request);
    }
    else if (!paramType.equals(HttpServletRequest.class) && !paramType.equals(HttpServletResponse.class)) {
        // C'est un objet complexe ! On peuple ses champs avec les données de la requête
        try {
            Object complexObj = paramType.getDeclaredConstructor().newInstance();
            for (java.lang.reflect.Field field : paramType.getDeclaredFields()) {
                field.setAccessible(true);
                String fieldName = field.getName();
                Class<?> fieldType = field.getType();
                
                if (fieldType.equals(FileUpload.class)) {
                    FileUpload f = extractFileUpload(fieldName, request, isMultipart);
                    if (f != null) field.set(complexObj, f);
                } else if (isSimpleType(fieldType)) {
                    Object val = extractSimpleValue(fieldName, fieldType, request);
                    if (val != null) {
                        field.set(complexObj, val);
                    }
                }
            }
            methodArgs[i] = complexObj;
        } catch (Exception e) {
            methodArgs[i] = null;
        }
    }
}
```

Pour fonctionner, ce code s'appuie sur trois nouvelles méthodes utilitaires (`isSimpleType`, `extractFileUpload`, `extractSimpleValue`) qui extraient proprement les données en vérifiant le typage (voir le code source de `FrontControllerServlet`).

## 3. Configuration de la compilation (CRITIQUE)

**Pourquoi cette modification ?**
Pour optimiser, Java efface le nom des arguments (`arg0`, `arg1`). Il **faut** compiler avec l'option `-parameters` pour que le framework lise "nom" ou "prenom" et puisse lier les données HTML à l'objet Java.

## 4. Test (Le Contrôleur)

Une fois implémenté, vous pouvez tester avec ce contrôleur et cet objet `User` :

```java
public class User {
    private String nom;
    private String prenom;
    private FileUpload photo;
    // ... getters et setters
}

@Controller
public class TestController {
    @UrlMapping(value = "/user/save", method = {"POST"})
    public String saveUser(User user) {
        // L'objet "user" est automatiquement rempli !
        return "Nom: " + user.getNom() + ", Fichier: " + user.getPhoto().getName();
    }
}
```

Et ce formulaire HTML dans votre `test-webapp` :

```html
<form action="/test-webapp/user/save" method="POST" enctype="multipart/form-data">
    Nom: <input type="text" name="nom" /><br/>
    Prenom: <input type="text" name="prenom" /><br/>
    Photo: <input type="file" name="photo" /><br/>
    <button type="submit">Enregistrer</button>
</form>
```
