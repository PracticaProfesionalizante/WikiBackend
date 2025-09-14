package com.teclab.practicas.WikiBackend.exception;

public class MenuPathExistente extends RuntimeException {
    public MenuPathExistente(String message) {
        super(message);
    }
}