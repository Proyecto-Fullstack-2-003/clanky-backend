package cl.clanky.carrito.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.clanky.carrito.model.Carrito;

public interface CarritoRepository
        extends JpaRepository<Carrito, Long> {

    Optional<Carrito> findByUsuarioId(Long usuarioId);
}
