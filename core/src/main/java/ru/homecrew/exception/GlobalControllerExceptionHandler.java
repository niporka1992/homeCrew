package ru.homecrew.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestControllerAdvice
public class GlobalControllerExceptionHandler {

    // 🔹 400 — Ошибка запроса (отсутствие параметров, биндинг и т.д.)
    @ExceptionHandler({
        MissingServletRequestParameterException.class,
        BindException.class,
        MethodArgumentNotValidException.class
    })
    public ResponseEntity<String> handleBadRequest(Exception ex) {
        log.warn("⚠️ Bad request: {}", ex.getMessage());
        String message = extractValidationMessage(ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(message != null ? message : "Bad request: " + ex.getMessage());
    }

    // 🔹 401 — Неавторизован
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<String> handleUnauthorized(AuthenticationException ex) {
        log.warn("🔒 Unauthorized: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: " + ex.getMessage());
    }

    // 🔹 403 — Доступ запрещён
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDenied(AccessDeniedException ex) {
        log.warn("🚫 Access denied: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
    }

    // 🔹 404 — Не найдено
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> handleResponseStatus(ResponseStatusException ex) {
        if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
            log.warn("❗ Not found: {}", ex.getReason());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found: " + ex.getReason());
        }
        return ResponseEntity.status(ex.getStatusCode())
                .body(ex.getReason() != null ? ex.getReason() : ex.getMessage());
    }

    // 🔹 405 — Неподдерживаемый метод
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<String> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        log.warn("🛑 Method not supported: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body("HTTP method not supported: " + ex.getMethod());
    }

    // 🔹 500 — Общий обработчик (всё, что не перехвачено выше)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleUnexpected(Exception ex) {
        log.error("💥 Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Internal server error: " + ex.getClass().getSimpleName());
    }

    private String extractValidationMessage(Exception ex) {
        if (ex instanceof MethodArgumentNotValidException manv) {
            return manv.getBindingResult().getFieldErrors().stream()
                    .map(err -> err.getField() + ": " + err.getDefaultMessage())
                    .reduce((a, b) -> a + "; " + b)
                    .orElse("Validation failed");
        }
        if (ex instanceof BindException be) {
            return be.getBindingResult().getFieldErrors().stream()
                    .map(err -> err.getField() + ": " + err.getDefaultMessage())
                    .reduce((a, b) -> a + "; " + b)
                    .orElse("Binding failed");
        }
        return null;
    }
}
