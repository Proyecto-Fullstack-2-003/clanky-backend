package cl.clanky.usuarios.repository;

import cl.clanky.usuarios.model.Usuario;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

@org.springframework.stereotype.Repository
public interface UsuariosRepository extends JpaRepository<Usuario, Long> {
  Optional<Usuario> findByUsuario(String usuario);

  Optional<Usuario> findByEmail(String email);

  boolean existsByUsuario(String usuario);

  boolean existsByEmail(String email);

  long countByRolAndActivoTrue(String rol);
}
