package cl.clanky.catalogo.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ProductoRequest(
    @NotBlank @Size(min = 3, max = 100) String nombre,
    @NotBlank @Pattern(regexp = "Vinilo|CD|Merch", message = "El formato debe ser Vinilo, CD o Merch")
        String formato,
    @NotNull @DecimalMin("1") @Digits(integer = 10, fraction = 0) BigDecimal precio,
    @NotNull @Min(0) @Max(1000000) Integer stock,
    @NotBlank @Size(max = 2048) @Pattern(regexp = "https?://[^\\s]+") String imagen) {}
