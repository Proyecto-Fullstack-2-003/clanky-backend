package cl.clanky.catalogo.dto;


import jakarta.validation.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.*;

public record VentaResponse(List<VentaLineaResponse> items, BigDecimal subtotal) {}
