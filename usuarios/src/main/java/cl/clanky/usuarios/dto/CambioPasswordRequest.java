package cl.clanky.usuarios.dto;

import jakarta.validation.constraints.*;

public record CambioPasswordRequest(
    @NotBlank String actual,
    @NotBlank @Pattern(regexp = RegistroRequest.CLAVE_SEGURA, message = RegistroRequest.MENSAJE_CLAVE)
        String nueva) {}
