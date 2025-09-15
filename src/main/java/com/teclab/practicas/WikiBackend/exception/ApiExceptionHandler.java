package com.teclab.practicas.WikiBackend.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice // <- para JSON
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiExceptionHandler {
//    DataIntegrityViolationException
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

    @ExceptionHandler(ConstraintViolationException.class) // @Validated en @RequestParam/@PathVariable
    public ProblemDetail handleParamValidation(ConstraintViolationException ex, HttpServletRequest req) {
        Map<String, String> errors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        v -> v.getPropertyPath().toString(),
                        ConstraintViolation::getMessage,   // forma más limpia
                        (a, b) -> a,                      // si hay duplicados, me quedo con el primero
                        LinkedHashMap::new                // mantiene orden de inserción
                ));

        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Parámetros inválidos");
        pd.setDetail("Uno o más parámetros no cumplen validación.");
        pd.setProperty("errors", errors);
        pd.setProperty("path", req.getRequestURI());
        return pd;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        pd.setTitle("Acceso Denegado");
        pd.setDetail("No tienes permiso para esta solicitud.");
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

    @ExceptionHandler(EmailIsExistente.class)
    public ProblemDetail handleEmailIsExistente(EmailIsExistente ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        pd.setTitle("Usuario Existente");
        pd.setDetail(ex.getMessage());
        pd.setProperty("path", req.getRequestURI());
        return pd;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
        pd.setTitle("Argumento Invalido");
        pd.setDetail(ex.getMessage());
        pd.setProperty("path", req.getRequestURI());
        return pd;
    }

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalStateException(IllegalStateException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
        pd.setTitle("Operacion Invalida");
        pd.setDetail(ex.getMessage());
        pd.setProperty("path", req.getRequestURI());
        return pd;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolationException(DataIntegrityViolationException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
        pd.setTitle("Argumento Invalido");
        pd.setDetail(ex.getLocalizedMessage());
        pd.setProperty("path", req.getRequestURI());
        return pd;
    }

    @ExceptionHandler(MenuPathExistente.class)
    public ProblemDetail handleMenuPathExistente(MenuPathExistente ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
        pd.setTitle("Path Duplicado");
        pd.setDetail(ex.getMessage());
        pd.setProperty("path", req.getRequestURI());
        return pd;
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail handleIllegalArgumentException(EntityNotFoundException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
        pd.setTitle("Database Error");
        pd.setDetail(ex.getMessage());
        pd.setProperty("path", req.getRequestURI());
        return pd;
    }

    @ExceptionHandler(Exception.class) // Fallback
    public ProblemDetail handleUnexpected(Exception ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        pd.setTitle("Error interno");
        pd.setDetail("Ocurrió un error inesperado. Intenta más tarde.");
        pd.setProperty("path", req.getRequestURI());
        return pd;
    }
}