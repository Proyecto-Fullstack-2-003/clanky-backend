package cl.clanky.catalogo.controller;

import cl.clanky.catalogo.dto.VentaItemRequest;
import cl.clanky.catalogo.dto.VentaResponse;
import cl.clanky.catalogo.service.CatalogoService;
import cl.clanky.common.exception.ApiException;
import io.swagger.v3.oas.annotations.Hidden;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@Hidden
@RestController
@RequestMapping("/internal/ventas")
public class InternalController {
  private final CatalogoService service;
  private final String key;

  public InternalController(CatalogoService s, @Value("${app.internal-key}") String k) {
    service = s;
    key = k;
  }

  @PostMapping("/{id}")
  public VentaResponse vender(
      @PathVariable UUID id,
      @RequestHeader(value = "X-Internal-Key", defaultValue = "") String provided,
      @RequestBody List<VentaItemRequest> items) {
    if (!MessageDigest.isEqual(
        key.getBytes(StandardCharsets.UTF_8), provided.getBytes(StandardCharsets.UTF_8)))
      throw new ApiException(403, "Servicio no autorizado.");
    return service.vender(id.toString(), items);
  }
}
