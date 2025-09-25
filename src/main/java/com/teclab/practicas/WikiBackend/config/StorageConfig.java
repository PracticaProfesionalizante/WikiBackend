package com.teclab.practicas.WikiBackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class StorageConfig {

    @Value("${storage.upload.dir}")
    private String uploadDir;
    
    @Bean
    public Path fileStorageLocation() {
        try {
            Path path = Paths.get(uploadDir).toAbsolutePath().normalize();
            
            Files.createDirectories(path);
            
            return path;
        } catch (IOException ex) {
            throw new RuntimeException("No se pudo inicializar el directorio de almacenamiento.", ex);
        }
    }
}