package cl.clanky.catalogo.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "productos")
public class Producto {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @Column(nullable = false)
  public String nombre;

  @Column(nullable = false, unique = true)
  public String nombreClave;

  @Column(nullable = false)
  public String formato;

  @Column(nullable = false, precision = 14, scale = 2)
  public BigDecimal precio;

  public int stock;

  @Column(length = 2048)
  public String imagen;

  public boolean activo = true;

  public Producto() {}
}
