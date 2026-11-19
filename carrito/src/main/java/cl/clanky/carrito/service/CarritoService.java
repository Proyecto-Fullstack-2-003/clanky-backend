package cl.clanky.carrito.service;

import cl.clanky.carrito.model.Carrito;
import cl.clanky.carrito.model.CarritoItem;
import cl.clanky.carrito.repository.CarritoRepository;
import cl.clanky.carrito.client.CatalogoClient;
import java.util.UUID;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CarritoService {

    private final CarritoRepository carritoRepository;
    private final CatalogoClient catalogoClient;

    public CarritoService(
        CarritoRepository carritoRepository,
        CatalogoClient catalogoClient) {
    this.carritoRepository = carritoRepository;
    this.catalogoClient = catalogoClient;
    }

    @Transactional
    public Carrito obtenerOCrearCarrito(Long usuarioId) {

        if (usuarioId == null || usuarioId <= 0) {
            throw new IllegalArgumentException(
                "El ID del usuario debe ser válido"
            );
        }

        return carritoRepository.findByUsuarioId(usuarioId)
            .orElseGet(() -> {
                Carrito carrito = new Carrito();
                carrito.setUsuarioId(usuarioId);
                return carritoRepository.save(carrito);
            });
    }

    @Transactional
    public void agregarProducto(
            Long usuarioId,
            Long productoId,
            Integer cantidad) {

        if (productoId == null || productoId <= 0) {
            throw new IllegalArgumentException(
                "El ID del producto debe ser válido"
            );
        }

        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException(
                "La cantidad debe ser mayor que cero"
            );
        }

        Carrito carrito = obtenerOCrearCarrito(usuarioId);

        for (CarritoItem item : carrito.getItems()) {
            if (item.getProductoId().equals(productoId)) {
                item.setCantidad(
                    Math.addExact(item.getCantidad(), cantidad)
                );

                carritoRepository.save(carrito);
                return;
            }
        }

        CarritoItem nuevoItem = new CarritoItem();
        nuevoItem.setProductoId(productoId);
        nuevoItem.setCantidad(cantidad);
        nuevoItem.setCarrito(carrito);

        carrito.getItems().add(nuevoItem);

        carritoRepository.save(carrito);
    }

    @Transactional
    public void eliminarProducto(
            Long usuarioId,
            Long productoId) {

        Carrito carrito = obtenerOCrearCarrito(usuarioId);

        carrito.getItems().removeIf(
            item -> item.getProductoId().equals(productoId)
        );

        carritoRepository.save(carrito);
    }
    @Transactional
public void modificarCantidad(
        Long usuarioId,
        Long productoId,
        Integer cantidad) {

    if (productoId == null || productoId <= 0) {
        throw new IllegalArgumentException(
            "El ID del producto debe ser válido"
        );
    }

    if (cantidad == null || cantidad <= 0) {
        throw new IllegalArgumentException(
            "La cantidad debe ser mayor que cero"
        );
    }

    Carrito carrito = obtenerOCrearCarrito(usuarioId);

    for (CarritoItem item : carrito.getItems()) {
        if (item.getProductoId().equals(productoId)) {
            item.setCantidad(cantidad);
            carritoRepository.save(carrito);
            return;
        }
    }

    throw new IllegalArgumentException(
        "El producto no existe en el carrito"
    );
}

    @Transactional
    public void vaciarCarrito(Long usuarioId) {

        Carrito carrito = obtenerOCrearCarrito(usuarioId);

        carrito.getItems().clear();

        carritoRepository.save(carrito);
    }



@Transactional
public CatalogoClient.VentaResponse comprarCarrito(Long usuarioId) {

    Carrito carrito = obtenerOCrearCarrito(usuarioId);

    if (carrito.getItems().isEmpty()) {
        throw new IllegalArgumentException(
            "No puedes comprar un carrito vacío"
        );
    }

    List<CatalogoClient.ProductoVentaRequest> productos =
        carrito.getItems().stream()
            .map(item -> new CatalogoClient.ProductoVentaRequest(
                item.getProductoId(),
                item.getCantidad()
            ))
            .toList();

    String ventaId = UUID.randomUUID().toString();

    CatalogoClient.VentaResponse resultado =
        catalogoClient.procesarVenta(ventaId, productos);

    carrito.getItems().clear();
    carritoRepository.save(carrito);

    return resultado;
}


}
