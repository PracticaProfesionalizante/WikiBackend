package com.teclab.practicas.WikiBackend.exception;

public class EmailIsExistente extends RuntimeException {
    public EmailIsExistente(String message) {
        super(message);
    }
}