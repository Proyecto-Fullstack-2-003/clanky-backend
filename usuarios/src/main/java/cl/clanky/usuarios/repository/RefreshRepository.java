package cl.clanky.usuarios.repository;

import cl.clanky.usuarios.model.RefreshSession;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;

@org.springframework.stereotype.Repository
public interface RefreshRepository extends JpaRepository<RefreshSession, String> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select s from RefreshSession s where s.tokenHash=:hash")
  Optional<RefreshSession> bloquear(String hash);

  void deleteByUsuarioId(Long usuarioId);

  void deleteByExpiraBefore(java.time.Instant limite);
}
