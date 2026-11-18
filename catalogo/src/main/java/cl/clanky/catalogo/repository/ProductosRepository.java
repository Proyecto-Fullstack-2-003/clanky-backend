package cl.clanky.catalogo.repository;

import cl.clanky.catalogo.model.Producto;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;

@org.springframework.stereotype.Repository
public interface ProductosRepository extends JpaRepository<Producto, Long> {
  List<Producto> findByActivoTrueOrderByIdAsc();

  boolean existsByNombreClaveAndActivoTrueAndIdNot(String nombre, Long id);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select p from Producto p where p.id=:id")
  Optional<Producto> bloquear(Long id);
}
