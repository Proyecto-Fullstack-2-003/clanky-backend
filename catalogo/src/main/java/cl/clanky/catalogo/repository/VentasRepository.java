package cl.clanky.catalogo.repository;

import cl.clanky.catalogo.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;

@org.springframework.stereotype.Repository
public interface VentasRepository extends JpaRepository<Venta, String> {}
