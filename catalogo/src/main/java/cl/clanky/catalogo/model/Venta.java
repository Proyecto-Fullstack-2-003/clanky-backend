package cl.clanky.catalogo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "ventas_inventario")
public class Venta {
  @Id public String pedidoId;

  @Column(nullable = false, length = 4096)
  public String huella;

  @Lob
  @Column(nullable = false)
  public String comprobante;

  public Venta() {}

  public Venta(String id, String hash, String receipt) {
    pedidoId = id;
    huella = hash;
    comprobante = receipt;
  }
}
