package com.teclab.practicas.WikiBackend.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice // <- para JSON
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDeniedException(AccessDeniedException ex) {
        // En un caso real, podrías devolver un objeto JSON más detallado
        return new ResponseEntity<>("Acceso denegado: " + ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class) // @Valid en @RequestBody
    public ProblemDetail handleBodyValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
        pd.setTitle("Datos inválidos");
        pd.setDetail("Uno o más campos no cumplen las validaciones.");
        pd.setProperty("errors", errors);
        pd.setProperty("path", req.getRequestURI());
        return pd;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class) // JSON mal formado / tipos erróneos
    public ProblemDetail handleUnreadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Cuerpo de la petición inválido");
        pd.setDetail("JSON mal formado o tipos de dato incorrectos.");
        pd.setProperty("path", req.getRequestURI());
        return pd;
    }

    @ExceptionHandler(Exception.class) // Fallback
    public ProblemDetail handleUnexpected(Exception ex, HttpServletRequest req) {
        // Aquí loguea el stacktrace con tu logger preferido
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        pd.setTitle("Error interno");
        pd.setDetail("Ocurrió un error inesperado. Intenta más tarde.");
        pd.setProperty("path", req.getRequestURI());
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    @ExceptionHandler(EmailIsExistente.class)
    public ProblemDetail handleEmailIsExistente(EmailIsExistente ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        pd.setTitle("Usuario Existente");
        pd.setDetail(ex.getMessage());
        pd.setProperty("path", req.getRequestURI());
        return pd;
    }
}