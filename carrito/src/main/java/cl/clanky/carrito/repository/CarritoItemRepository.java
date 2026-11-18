package cl.clanky.carrito.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.clanky.carrito.model.CarritoItem;

public interface CarritoItemRepository
        extends JpaRepository<CarritoItem, Long> {
}
