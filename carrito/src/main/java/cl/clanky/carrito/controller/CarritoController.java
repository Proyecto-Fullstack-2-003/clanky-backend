package cl.clanky.carrito.controller;

import cl.clanky.carrito.model.Carrito;
import cl.clanky.carrito.model.CarritoItem;
import cl.clanky.carrito.repository.CarritoRepository;
import cl.clanky.carrito.service.CarritoService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/carrito")
public class CarritoController {

    private final CarritoService service;
    private final CarritoRepository repository;

    public CarritoController(
            CarritoService service,
            CarritoRepository repository) {
        this.service = service;
        this.repository = repository;
    }

    @GetMapping("/{usuarioId}")
    public ResponseEntity<?> obtener(@PathVariable Long usuarioId) {
        Carrito carrito = service.obtenerOCrearCarrito(usuarioId);

        List<ItemResponse> items = carrito.getItems().stream()
            .map(item -> new ItemResponse(
                item.getProductoId(),
                item.getCantidad()
            ))
            .toList();

        return ResponseEntity.ok(items);
    }

    @PostMapping("/{usuarioId}/items")
    public ResponseEntity<String> agregar(
            @PathVariable Long usuarioId,
            @RequestBody AgregarItemRequest request) {

        service.agregarProducto(
            usuarioId,
            request.productoId(),
            request.cantidad()
        );

        return ResponseEntity.ok("Producto agregado al carrito");
    }

    @DeleteMapping("/{usuarioId}/items/{productoId}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long usuarioId,
            @PathVariable Long productoId) {

        service.eliminarProducto(usuarioId, productoId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{usuarioId}")
    public ResponseEntity<Void> vaciar(@PathVariable Long usuarioId) {
        service.vaciarCarrito(usuarioId);
        return ResponseEntity.noContent().build();
    }

    public record AgregarItemRequest(
        Long productoId,
        Integer cantidad
    ) {}

    public record ItemResponse(
        Long productoId,
        Integer cantidad
    ) {}

    @PutMapping("/{usuarioId}/items/{productoId}")
    public ResponseEntity<String> modificarCantidad(
            @PathVariable Long usuarioId,
            @PathVariable Long productoId,
            @RequestBody ModificarCantidadRequest request) {

        service.modificarCantidad(
            usuarioId,
            productoId,
            request.cantidad()
        );

        return ResponseEntity.ok("Cantidad actualizada");
    }

    public record ModificarCantidadRequest(
        Integer cantidad
    ) {}

}
