package cl.clanky.usuarios.model;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class Usuario {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @Column(nullable = false, unique = true)
  public String usuario;

  @Column(nullable = false)
  public String nombre;

  @Column(nullable = false, unique = true)
  public String email;

  @Column(nullable = false)
  public String passwordHash;

  @Column(nullable = false)
  public String rol = "CLIENTE";

  public boolean activo = true;

  // Protección contra fuerza bruta: se bloquea el login tras varios intentos fallidos.
  public Integer intentosFallidos = 0;

  public java.time.Instant bloqueadoHasta;

  public Usuario() {}
}
