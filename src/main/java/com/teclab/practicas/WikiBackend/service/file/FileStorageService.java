package com.teclab.practicas.WikiBackend.service.file;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    /** Guarda el archivo y devuelve el path o URL de acceso. */
    String storeFile(MultipartFile file);
    
    /** Carga el archivo como un recurso para la descarga. */
    Resource loadFileAsResource(String filePath);
    
    /** Elimina el archivo. */
    void deleteFile(String filePath);
}