package cl.clanky.catalogo.dto;

import jakarta.validation.*;
import jakarta.validation.constraints.*;
import java.util.*;

public record VentaItemRequest(Long id, int cantidad) {}
