
package cl.clanky.carrito.controller;

import cl.clanky.carrito.model.Carrito;
import cl.clanky.carrito.service.CarritoService;
import cl.clanky.carrito.client.CatalogoClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@RestController
@RequestMapping("/api/v1/carrito")
public class CarritoController {

    private final CarritoService service;

    public CarritoController(CarritoService service) {
        this.service = service;
    }

    // Comprueba que el usuario de la URL sea el dueño del token.
    private Long validarUsuario(Long usuarioId, Jwt jwt) {

        Long usuarioAutenticado = Long.valueOf(jwt.getSubject());

        if (!usuarioAutenticado.equals(usuarioId)) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "No puedes acceder al carrito de otro usuario"
            );
        }

        return usuarioAutenticado;
    }

    @GetMapping("/{usuarioId}")
    @Transactional
    public ResponseEntity<List<ItemResponse>> obtener(
            @PathVariable Long usuarioId,
            @AuthenticationPrincipal Jwt jwt) {

        Long id = validarUsuario(usuarioId, jwt);

        Carrito carrito = service.obtenerOCrearCarrito(id);

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
            @RequestBody AgregarItemRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        Long id = validarUsuario(usuarioId, jwt);

        service.agregarProducto(
            id,
            request.productoId(),
            request.cantidad()
        );

        return ResponseEntity.ok("Producto agregado al carrito");
    }


    @PutMapping("/{usuarioId}/items/{productoId}")
    public ResponseEntity<String> modificarCantidad(
            @PathVariable Long usuarioId,
            @PathVariable Long productoId,
            @RequestBody ModificarCantidadRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        Long id = validarUsuario(usuarioId, jwt);

        service.modificarCantidad(
            id,
            productoId,
            request.cantidad()
        );

        return ResponseEntity.ok("Cantidad modificada correctamente");
    }


    @DeleteMapping("/{usuarioId}/items/{productoId}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long usuarioId,
            @PathVariable Long productoId,
            @AuthenticationPrincipal Jwt jwt) {

        Long id = validarUsuario(usuarioId, jwt);

        service.eliminarProducto(id, productoId);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{usuarioId}")
    public ResponseEntity<Void> vaciar(
            @PathVariable Long usuarioId,
            @AuthenticationPrincipal Jwt jwt) {

        Long id = validarUsuario(usuarioId, jwt);

        service.vaciarCarrito(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{usuarioId}/comprar")
public ResponseEntity<CatalogoClient.VentaResponse> comprar(
        @PathVariable Long usuarioId,
        @AuthenticationPrincipal Jwt jwt) {

    Long id = validarUsuario(usuarioId, jwt);

    CatalogoClient.VentaResponse resultado =
        service.comprarCarrito(id);

    return ResponseEntity.ok(resultado);
}

    public record AgregarItemRequest(
        Long productoId,
        Integer cantidad
    ) {}

    public record ModificarCantidadRequest(
        Integer cantidad
    ) {}

    public record ItemResponse(
        Long productoId,
        Integer cantidad
    ) {}
}
