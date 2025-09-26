package com.teclab.practicas.WikiBackend.service.file;

import com.teclab.practicas.WikiBackend.exception.FileStorageException;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final Path fileStorageLocation;

    @Autowired
    public FileStorageServiceImpl(Path fileStorageLocation) {
        this.fileStorageLocation = fileStorageLocation;
    }

    @Override
    public String storeFile(MultipartFile file) {
        // 1. Generar un nombre de archivo único y seguro.
        String fileName = StringUtils.cleanPath(UUID.randomUUID() + "_" + file.getOriginalFilename());

        try {
            // 2. Copiar el stream del archivo al destino del disco.
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            return fileName;

        } catch (IOException ex) {
            throw new FileStorageException("No se pudo guardar el archivo " + fileName, ex);
        }
    }

    @Override
    public Resource loadFileAsResource(String filePath) {
        return null;
    }

    @Override
    public void deleteFile(String filePath) {

    }

    // ... Implementación de loadFileAsResource y deleteFile
}