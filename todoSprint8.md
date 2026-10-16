# Guide du Sprint 8 : Emploi d'un fichier en utilisant classe Bit

Ce document explique comment mettre en place la gestion des fichiers binaires en utilisant une classe Bit pour stocker et manipuler les données binaires.

## Objectif
Créer une classe `Bit` pour représenter et manipuler des données binaires (fichiers) de manière optimisée.

## 1. Création de la classe `Bit`

La classe `Bit` encapsulera les données binaires et fournira des méthodes utilitaires pour la manipulation.

```java
package util;

public class Bit {
    private byte[] data;
    private String filename;
    private String contentType;
    private long size;
    
    public Bit() {}
    
    public Bit(byte[] data, String filename, String contentType) {
        this.data = data;
        this.filename = filename;
        this.contentType = contentType;
        this.size = data != null ? data.length : 0;
    }
    
    // Getters et Setters
    public byte[] getData() { return data; }
    public void setData(byte[] data) { 
        this.data = data; 
        this.size = data != null ? data.length : 0;
    }
    
    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }
    
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    
    public long getSize() { return size; }
    
    // Méthodes utilitaires
    public boolean isEmpty() {
        return data == null || data.length == 0;
    }
    
    public String getExtension() {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf("."));
    }
}
```

## 2. Intégration dans le framework

Modifier `ProcessRequest` pour utiliser la classe `Bit` lors de l'extraction des fichiers uploadés.

## 3. Exemple d'utilisation

Créer un contrôleur qui utilise la classe `Bit` :

```java
@Controller
public class FileController {
    
    @PostMapping(url = "/upload")
    public String upload(Bit file) {
        if (!file.isEmpty()) {
            // Traitement du fichier
            return "Fichier reçu: " + file.getFilename() + 
                   " (" + file.getSize() + " bytes)";
        }
        return "Aucun fichier reçu";
    }
}
```

## 4. Tests

Créer un formulaire HTML pour tester l'upload :

```html
<form action="/upload" method="POST" enctype="multipart/form-data">
    <input type="file" name="file" />
    <button type="submit">Envoyer</button>
</form>
```
