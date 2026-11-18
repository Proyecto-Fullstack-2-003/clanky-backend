package cl.clanky.usuarios.dto;

import jakarta.validation.constraints.*;

public record RegistroRequest(
    @NotBlank @Pattern(regexp = "[a-zA-Z0-9_.-]{3,30}") String usuario,
    @NotBlank @Size(max = 100) String nombre,
    @NotBlank @Email String email,
    @NotBlank @Pattern(regexp = CLAVE_SEGURA, message = MENSAJE_CLAVE) String contrasena) {

  /** Política de contraseñas: 8 a 72 caracteres con mayúscula, minúscula, número y símbolo. */
  public static final String CLAVE_SEGURA =
      "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,72}$";

  public static final String MENSAJE_CLAVE =
      "Debe tener entre 8 y 72 caracteres con mayúscula, minúscula, número y símbolo";
}
