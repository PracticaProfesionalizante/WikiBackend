package com.teclab.practicas.WikiBackend.exception;

import java.io.IOException;

public class FileStorageException extends RuntimeException {
    public FileStorageException(String message, IOException ex) {
        super(message + ex.getMessage());
    }
}