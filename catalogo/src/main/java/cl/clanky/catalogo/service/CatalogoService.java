package cl.clanky.catalogo.service;

import cl.clanky.catalogo.dto.ProductoRequest;
import cl.clanky.catalogo.dto.ProductoResponse;
import cl.clanky.catalogo.dto.VentaItemRequest;
import cl.clanky.catalogo.dto.VentaLineaResponse;
import cl.clanky.catalogo.dto.VentaResponse;
import cl.clanky.catalogo.model.Producto;
import cl.clanky.catalogo.model.Venta;
import cl.clanky.catalogo.repository.ProductosRepository;
import cl.clanky.catalogo.repository.VentasRepository;
import cl.clanky.common.exception.ApiException;
import tools.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CatalogoService {

  private final ProductosRepository products;
  private final VentasRepository sales;
  private final ObjectMapper json;

  public CatalogoService(ProductosRepository p, VentasRepository s, ObjectMapper j) {
    products = p;
    sales = s;
    json = j;
  }

  public static ProductoResponse vista(Producto p) {
    return new ProductoResponse(p.id, p.nombre, p.formato, p.precio, p.stock, p.imagen);
  }

  public static String normalizar(String s) {
    return Normalizer.normalize(s.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
        .replaceAll("\\p{M}", "");
  }

  public static final Set<String> FORMATOS = Set.of("Vinilo", "CD", "Merch");

  public List<ProductoResponse> listar(String formato, String texto) {
    if (formato != null && !formato.isBlank() && !FORMATOS.contains(formato.trim()))
      throw new ApiException(400, "El formato debe ser Vinilo, CD o Merch.");
    String buscado = texto == null ? "" : normalizar(texto);
    return products.findByActivoTrueOrderByIdAsc().stream()
        .filter(p -> formato == null || formato.isBlank() || p.formato.equals(formato.trim()))
        .filter(p -> buscado.isEmpty() || p.nombreClave.contains(buscado))
        .map(CatalogoService::vista)
        .toList();
  }

  public ProductoResponse obtener(Long id) {
    return vista(
        products
            .findById(id)
            .filter(p -> p.activo)
            .orElseThrow(() -> new ApiException(404, "Producto no encontrado.")));
  }

  @Transactional
  public ProductoResponse guardar(Long id, ProductoRequest f) {
    String key = normalizar(f.nombre());
    if (products.existsByNombreClaveAndActivoTrueAndIdNot(key, id == null ? -1L : id))
      throw new ApiException(409, "Ya existe un producto con ese nombre.");
    Producto p =
        id == null
            ? new Producto()
            : products
                .bloquear(id)
                .filter(x -> x.activo)
                .orElseThrow(() -> new ApiException(404, "Producto no encontrado."));
    p.nombre = f.nombre().trim();
    p.nombreClave = key;
    p.formato = f.formato().trim();
    p.precio = f.precio();
    p.stock = f.stock();
    p.imagen = f.imagen().trim();
    return vista(products.saveAndFlush(p));
  }

  @Transactional
  public void eliminar(Long id) {
    Producto p =
        products
            .bloquear(id)
            .filter(x -> x.activo)
            .orElseThrow(() -> new ApiException(404, "Producto no encontrado."));
    p.activo = false;
    p.nombreClave = p.nombreClave + "#eliminado-" + p.id;
    products.save(p);
  }

  @Transactional
  public VentaResponse vender(String pedido, List<VentaItemRequest> items) {
    if (items == null
        || items.isEmpty()
        || items.size() > 50
        || items.stream().anyMatch(i -> i.id() == null || i.cantidad() < 1)
        || items.stream().map(VentaItemRequest::id).distinct().count() != items.size())
      throw new ApiException(
          400, "La compra requiere productos únicos y cantidades enteras positivas.");
    List<VentaItemRequest> ordered =
        items.stream().sorted(Comparator.comparing(VentaItemRequest::id)).toList();
    String fingerprint = ordered.toString();
    Optional<Venta> existing = sales.findById(pedido);
    if (existing.isPresent()) {
      if (!existing.get().huella.equals(fingerprint))
        throw new ApiException(409, "El pedido ya fue utilizado con otros productos.");
      return leer(existing.get().comprobante);
    }
    List<VentaLineaResponse> lines = new ArrayList<>();
    BigDecimal subtotal = BigDecimal.ZERO;
    for (VentaItemRequest i : ordered) {
      Producto p =
          products
              .bloquear(i.id())
              .filter(x -> x.activo)
              .orElseThrow(() -> new ApiException(409, "Un producto ya no está disponible."));
      if (p.stock < i.cantidad())
        throw new ApiException(409, "Stock insuficiente de " + p.nombre + ".");
      p.stock -= i.cantidad();
      lines.add(new VentaLineaResponse(p.id, p.nombre, p.precio, i.cantidad()));
      subtotal = subtotal.add(p.precio.multiply(BigDecimal.valueOf(i.cantidad())));
    }
    VentaResponse receipt = new VentaResponse(lines, subtotal);
    sales.saveAndFlush(new Venta(pedido, fingerprint, escribir(receipt)));
    return receipt;
  }

  private String escribir(VentaResponse c) {
    try {
      return json.writeValueAsString(c);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  private VentaResponse leer(String c) {
    try {
      return json.readValue(c, VentaResponse.class);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }
}
