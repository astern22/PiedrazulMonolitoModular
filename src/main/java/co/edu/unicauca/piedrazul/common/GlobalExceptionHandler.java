package co.edu.unicauca.piedrazul.common;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Traduce las excepciones a respuestas JSON con un campo "message" legible,
 * que es el que el frontend muestra al usuario.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        return body(HttpStatus.BAD_REQUEST, "Datos invalidos. " + detail);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleIntegrity(DataIntegrityViolationException ex) {
        return body(
                HttpStatus.CONFLICT,
                "No se pudo guardar: algun dato ya existe o hace referencia a un registro inexistente."
        );
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleStatus(ResponseStatusException ex) {
        String reason = ex.getReason() != null ? ex.getReason() : "Error en la solicitud";
        return body(HttpStatus.valueOf(ex.getStatusCode().value()), reason);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntime(RuntimeException ex) {
        // La seguridad de Spring debe seguir resolviendo 401/403 por su cuenta.
        if (ex instanceof AccessDeniedException || ex instanceof AuthenticationException) {
            throw ex;
        }

        // Errores de negocio lanzados por los servicios con un mensaje pensado para el usuario.
        boolean businessError = ex.getClass() == RuntimeException.class
                || ex instanceof IllegalArgumentException
                || ex instanceof IllegalStateException;

        if (businessError && ex.getMessage() != null && !ex.getMessage().isBlank()) {
            return body(HttpStatus.BAD_REQUEST, ex.getMessage());
        }

        return body(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error inesperado en el servidor.");
    }

    private ResponseEntity<Map<String, String>> body(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("message", message));
    }
}
