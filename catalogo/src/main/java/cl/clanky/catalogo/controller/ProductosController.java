package cl.clanky.catalogo.controller;

import cl.clanky.catalogo.dto.ProductoRequest;
import cl.clanky.catalogo.dto.ProductoResponse;
import cl.clanky.catalogo.service.CatalogoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Productos", description = "Catálogo de vinilos, CDs y merch. Lectura pública; escritura solo ADMIN")
@RestController
@RequestMapping("/api/productos")
public class ProductosController {

  private final CatalogoService service;

  public ProductosController(CatalogoService s) {
    service = s;
  }

  @Operation(summary = "Listar productos; filtros opcionales por formato (Vinilo, CD, Merch) y texto")
  @GetMapping
  public List<ProductoResponse> listar(
      @RequestParam(required = false) String formato, @RequestParam(required = false) String q) {
    return service.listar(formato, q);
  }

  @Operation(summary = "Obtener un producto por id")
  @GetMapping("/{id}")
  public ProductoResponse obtener(@PathVariable Long id) {
    return service.obtener(id);
  }

  @Operation(summary = "Crear un producto (ADMIN)")
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ProductoResponse crear(@Valid @RequestBody ProductoRequest f) {
    return service.guardar(null, f);
  }

  @Operation(summary = "Actualizar un producto (ADMIN)")
  @PutMapping("/{id}")
  public ProductoResponse actualizar(@PathVariable Long id, @Valid @RequestBody ProductoRequest f) {
    return service.guardar(id, f);
  }

  @Operation(summary = "Eliminar un producto con baja lógica (ADMIN)")
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void eliminar(@PathVariable Long id) {
    service.eliminar(id);
  }
}
