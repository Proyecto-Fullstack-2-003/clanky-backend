
package cl.clanky.carrito.service;
import cl.clanky.carrito.model.CarritoItem;
import cl.clanky.carrito.model.Carrito;
import cl.clanky.carrito.repository.CarritoRepository;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CarritoServiceTest {

    @Test
    void noDebePermitirCantidadCero() {

        CarritoRepository repository =
            mock(CarritoRepository.class);

        CarritoService service =
            new CarritoService(repository);

        assertThrows(
            IllegalArgumentException.class,
            () -> service.agregarProducto(1L, 1L, 0)
        );
    }

    @Test
    void debeAgregarProductoAlCarrito() {

        CarritoRepository repository =
            mock(CarritoRepository.class);

        Carrito carrito = new Carrito();
        carrito.setUsuarioId(1L);

        when(repository.findByUsuarioId(1L))
            .thenReturn(Optional.of(carrito));

        CarritoService service =
            new CarritoService(repository);

        service.agregarProducto(1L, 10L, 2);

        assertEquals(1, carrito.getItems().size());
        assertEquals(
            10L,
            carrito.getItems().get(0).getProductoId()
        );
        assertEquals(
            2,
            carrito.getItems().get(0).getCantidad()
        );

        verify(repository).save(carrito);
    }

    @Test
    void debeModificarCantidadDeProducto() {

        CarritoRepository repository =
            mock(CarritoRepository.class);

        Carrito carrito = new Carrito();
        carrito.setUsuarioId(1L);

        CarritoItem item = new CarritoItem();
        item.setProductoId(10L);
        item.setCantidad(2);
        item.setCarrito(carrito);

        carrito.getItems().add(item);

        when(repository.findByUsuarioId(1L))
            .thenReturn(Optional.of(carrito));

        CarritoService service =
            new CarritoService(repository);

        service.modificarCantidad(1L, 10L, 5);

        assertEquals(5, item.getCantidad());
        verify(repository).save(carrito);
    }

    @Test
    void debeEliminarProductoDelCarrito() {

        CarritoRepository repository =
            mock(CarritoRepository.class);

        Carrito carrito = new Carrito();
        carrito.setUsuarioId(1L);

        CarritoItem item = new CarritoItem();
        item.setProductoId(10L);
        item.setCantidad(2);
        item.setCarrito(carrito);

        carrito.getItems().add(item);

        when(repository.findByUsuarioId(1L))
            .thenReturn(Optional.of(carrito));

        CarritoService service =
            new CarritoService(repository);

        service.eliminarProducto(1L, 10L);

        assertTrue(carrito.getItems().isEmpty());
        verify(repository).save(carrito);
    }

}
