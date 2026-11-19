
package cl.clanky.carrito.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> manejarArgumentoInvalido(
            IllegalArgumentException ex) {

        Map<String, String> respuesta = Map.of(
            "error", ex.getMessage()
        );

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(respuesta);
    }
    @ExceptionHandler(org.springframework.web.client.HttpClientErrorException.class)
public ResponseEntity<Map<String, Object>> manejarErrorCatalogo(
        org.springframework.web.client.HttpClientErrorException ex) {

    Map<String, Object> respuesta = Map.of(
        "status", ex.getStatusCode().value(),
        "mensaje", ex.getStatusText()
    );

    return ResponseEntity
        .status(ex.getStatusCode())
        .body(respuesta);
}
}
