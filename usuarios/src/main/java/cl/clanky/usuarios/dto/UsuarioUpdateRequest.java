package cl.clanky.usuarios.dto;

import jakarta.validation.*;
import jakarta.validation.constraints.*;
import java.util.*;

public record UsuarioUpdateRequest(
    @NotBlank @Size(max = 100) String nombre,
    @Email @NotBlank String email,
    @Pattern(regexp = "ADMIN|CLIENTE") @NotBlank String rol) {}
