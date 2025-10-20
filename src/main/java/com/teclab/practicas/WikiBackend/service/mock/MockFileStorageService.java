package com.teclab.practicas.WikiBackend.service.mock;

import com.teclab.practicas.WikiBackend.exception.FileStorageException;
import com.teclab.practicas.WikiBackend.service.file.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Profile({"dev", "test"})
public class MockFileStorageService implements FileStorageService {

    // Simulación: usar un Map para 'guardar' temporalmente los archivos en memoria.
    // KEY: FileId (String), VALUE: Los bytes del archivo (byte[])
    private final Map<String, byte[]> fileStorage = new HashMap<>();

    @Value("${app.maxFileSize:10485760}") // Ejemplo de tamaño maximo: 10 MB (10×1024×1024)
    private long maxFileSize;

    @Autowired
    public MockFileStorageService(){}
    
    // --- Implementación de storeFile ---
    @Override
    public String storeFile(MultipartFile file) {
        // 1. Validaciones
        if (file.isEmpty()) {
            throw new FileStorageException("El archivo es obligatorio.");
        }
        if (file.getSize() > maxFileSize) {
            // Tamaño máximo configurable (ej.: 50 MB)
            throw new FileStorageException("El tamaño del archivo excede el límite configurado (" + maxFileSize + " bytes).");
        }
        if (!"application/pdf".equals(file.getContentType())) {
            // Solo archivos PDF permitidos 
            throw new FileStorageException("Solo se permiten archivos de tipo PDF.");
        }

        try {
            // 2. Simulación de almacenamiento
            String fileId = UUID.randomUUID() + "-" + file.getOriginalFilename();
            fileStorage.put(fileId, file.getBytes()); // <--- IOException aquí

            System.out.println("Mock Storage: Archivo simulado guardado en: " + fileId);
            return fileId;
        } catch (IOException e) {
            throw new FileStorageException("Error al procesar el archivo: " + e.getMessage());
        }
    }

    // --- Implementación de loadFileAsResource ---
    @Override
    public Resource loadFileAsResource(String filePath) {
        if (!fileStorage.containsKey(filePath)) {
            // En un Mock, puedes devolver null o lanzar una excepción,
            // pero lanzar la excepción es mejor para la lógica de negocio.
            throw new FileStorageException("El archivo con ID " + filePath + " no fue encontrado en el Mock Storage.");
        }

        // Obtener los bytes del mapa
        byte[] fileBytes = fileStorage.get(filePath);

        // CREAR el ByteArrayResource a partir de los bytes
        return new ByteArrayResource(fileBytes);
    }

    // Implementación de deleteFile
    @Override
    public void deleteFile(String filePath) {
        boolean isRemoved = fileStorage.remove(filePath) != null;
        if (!isRemoved) {
            throw new FileStorageException("El archivo con ID " + filePath + " no fue encontrado en el Mock Storage.");
        }
    }
}