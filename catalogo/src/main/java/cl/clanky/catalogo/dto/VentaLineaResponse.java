package cl.clanky.catalogo.dto;

import jakarta.validation.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.*;

public record VentaLineaResponse(Long id, String nombre, BigDecimal precio, int cantidad) {}
