package br.com.fabrisio.cepapi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice(assignableTypes = CepController.class)
public class CepErrorHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handle(ResponseStatusException exception) {
        var status = exception.getStatus();
        String code = switch (status) {
            case BAD_REQUEST -> "INVALID_CEP";
            case NOT_FOUND -> "CEP_NOT_FOUND";
            case BAD_GATEWAY -> "PROVIDER_UNAVAILABLE";
            default -> "REQUEST_FAILED";
        };
        return ResponseEntity.status(status)
                .body(new ApiError(status.value(), code, exception.getReason()));
    }

    public record ApiError(int status, String code, String message) {}
}
