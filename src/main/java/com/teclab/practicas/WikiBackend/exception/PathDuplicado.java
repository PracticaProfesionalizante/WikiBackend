package com.teclab.practicas.WikiBackend.exception;

public class PathDuplicado extends RuntimeException {
    public PathDuplicado(String message) {
        super(message);
    }
}