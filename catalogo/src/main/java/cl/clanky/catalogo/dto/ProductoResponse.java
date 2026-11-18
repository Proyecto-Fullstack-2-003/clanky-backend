package cl.clanky.catalogo.dto;

import java.math.BigDecimal;

public record ProductoResponse(
    Long id, String nombre, String formato, BigDecimal precio, int stock, String imagen) {}
