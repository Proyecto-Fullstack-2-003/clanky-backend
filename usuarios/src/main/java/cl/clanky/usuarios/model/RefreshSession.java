package cl.clanky.usuarios.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "sesiones_refresh")
public class RefreshSession {
  @Id public String tokenHash;
  public Long usuarioId;
  public Instant expira;

  public RefreshSession() {}

  public RefreshSession(String hash, Long user, Instant expiry) {
    tokenHash = hash;
    usuarioId = user;
    expira = expiry;
  }
}
