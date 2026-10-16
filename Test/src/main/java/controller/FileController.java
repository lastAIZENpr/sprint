package main.java.controller;

import annotation.*;
import util.Bit;

@Controller
@WebApi
public class FileController {

    @PostMapping(url = "/upload")
    public String upload(Bit file) {
        if (file != null && !file.isEmpty()) {
            return "Fichier reçu: " + file.getFilename() + 
                   " (" + file.getSize() + " bytes)" +
                   " - Type: " + file.getContentType() +
                   " - Extension: " + file.getExtension();
        }
        return "Aucun fichier reçu";
    }
}
