package com.teclab.practicas.WikiBackend.service.file;

import com.teclab.practicas.WikiBackend.exception.FileStorageException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
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
        if (filePath == null || filePath.isBlank()) {
            throw new FileStorageException("La ruta del archivo a cargar no puede ser vacía");
        }
        try {
            Path targetLocation = this.fileStorageLocation.resolve(filePath).normalize();
            // Evitar path traversal: asegurar que permanezca dentro de la carpeta base
            if (!targetLocation.startsWith(this.fileStorageLocation)) {
                throw new FileStorageException("Intento de acceso a una ruta fuera del directorio permitido");
            }

            Resource resource = new UrlResource(targetLocation.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new FileStorageException("Archivo no encontrado o no legible: " + filePath);
            }
        } catch (MalformedURLException ex) {
            throw new FileStorageException("URL de archivo inválida: " + filePath, ex);
        }
    }

    @Override
    public void deleteFile(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            throw new FileStorageException("La ruta del archivo a eliminar no puede ser vacía");
        }
        try {
            // Resolver de forma segura dentro del directorio base
            Path targetLocation = this.fileStorageLocation.resolve(filePath).normalize();
            // Evitar path traversal: asegurar que permanezca dentro de la carpeta base
            if (!targetLocation.startsWith(this.fileStorageLocation)) {
                throw new FileStorageException("Intento de acceso a una ruta fuera del directorio permitido");
            }

            // Borrar si existe (no falla si no existe)
            Files.deleteIfExists(targetLocation);
        } catch (IOException ex) {
            throw new FileStorageException("No se pudo eliminar el archivo: " + filePath, ex);
        }
    }
}