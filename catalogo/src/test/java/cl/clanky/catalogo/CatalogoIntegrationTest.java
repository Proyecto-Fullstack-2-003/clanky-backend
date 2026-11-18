package cl.clanky.catalogo;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import cl.clanky.catalogo.dto.ProductoRequest;
import cl.clanky.catalogo.dto.ProductoResponse;
import cl.clanky.catalogo.dto.VentaItemRequest;
import cl.clanky.catalogo.repository.ProductosRepository;
import cl.clanky.catalogo.service.CatalogoService;
import cl.clanky.common.exception.ApiException;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@org.springframework.test.context.ActiveProfiles("h2")
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:catalog-test")
@AutoConfigureMockMvc
class CatalogoIntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired CatalogoService service;
  @Autowired ProductosRepository repo;

  private static RequestPostProcessor jwtConRol(String rol) {
    return SecurityMockMvcRequestPostProcessors.jwt()
        .authorities(new SimpleGrantedAuthority("ROLE_" + rol));
}

  ProductoResponse producto(String nombre, int stock) {
    return service.guardar(
        null,
        new ProductoRequest(
            nombre, "CD", BigDecimal.valueOf(15000), stock, "https://example.com/p.jpg"));
  }

  @Test
  void catalogoEsPublico() throws Exception {
    mvc.perform(get("/api/productos"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].nombre").exists());
  }

  @Test
  void crearSinSesionDevuelve401() throws Exception {
    mvc.perform(post("/api/productos").contentType("application/json").content("{}"))
        .andExpect(status().isUnauthorized());
  }

@Test
void clienteNoPuedeEliminar() throws Exception {
    mvc.perform(delete("/api/productos/1").with(jwtConRol("CLIENTE")))
        .andExpect(status().isForbidden());
}

@Test
void adminDebeEnviarDatosValidos() throws Exception {
    mvc.perform(
            post("/api/productos")
                .with(jwtConRol("ADMIN"))
                .contentType("application/json")
                .content(
                    "{\"nombre\":\"x\",\"formato\":\"x\",\"precio\":-1,\"stock\":-1,\"imagen\":\"mal\"}"
                ))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.campos.precio").exists());
}

  @Test
  void precioLoDeterminaCatalogoEIdempotenciaEvitaDobleDescuento() {
    var p = producto("Idempotencia", 3);
    var items = List.of(new VentaItemRequest(p.id(), 2));
    String id = UUID.randomUUID().toString();
    var uno = service.vender(id, items);
    var dos = service.vender(id, items);
    assertEquals(0, uno.subtotal().compareTo(BigDecimal.valueOf(30000)));
    assertEquals(uno, dos);
    assertEquals(1, service.obtener(p.id()).stock());
  }

  @Test
  void siFallaUnProductoNoSeDescuentaElOtro() {
    var a = producto("Atomicidad A", 3);
    var b = producto("Atomicidad B", 0);
    assertThrows(
        ApiException.class,
        () ->
            service.vender(
                UUID.randomUUID().toString(),
                List.of(new VentaItemRequest(a.id(), 1), new VentaItemRequest(b.id(), 1))));
    assertEquals(3, service.obtener(a.id()).stock());
  }

  @Test
  void duplicadosIgnoranAcentosYMayusculas() {
    producto("Ratón Ágil", 2);
    assertThrows(ApiException.class, () -> producto("  RATON AGIL  ", 1));
  }

  @Test
  void bajaEsLogica() {
    var p = producto("Baja lógica", 1);
    service.eliminar(p.id());
    assertTrue(repo.findById(p.id()).isPresent());
    assertThrows(ApiException.class, () -> service.obtener(p.id()));
  }

  @Test
  void ventaRechazaIdsDuplicados() {
    var p = producto("Duplicado venta", 2);
    assertThrows(
        ApiException.class,
        () ->
            service.vender(
                UUID.randomUUID().toString(),
                List.of(new VentaItemRequest(p.id(), 1), new VentaItemRequest(p.id(), 1))));
  }

  @Test
  void adviceProductoInexistenteEntrega404ConJson() throws Exception {
    mvc.perform(get("/api/productos/9999999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.mensaje").value("Producto no encontrado."));
  }

  @Test
void adviceJsonMalFormadoEntrega400() throws Exception {
    mvc.perform(
            post("/api/productos")
                .with(jwtConRol("ADMIN"))
                .contentType("application/json")
                .content("{invalido"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.mensaje").value("Formato de solicitud inválido."));
}

  @Test
  void filtraPorFormatoYTexto() throws Exception {
    mvc.perform(get("/api/productos").param("formato", "Merch"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].formato").value("Merch"));
    mvc.perform(get("/api/productos").param("q", "pink floyd"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].nombre").value("PINK FLOYD - DARK SIDE OF THE MOON"));
    mvc.perform(get("/api/productos").param("formato", "Casete")).andExpect(status().isBadRequest());
  }

  @Test
  void seedCargaLosCincuentaYCincoProductosDeClanky() {
    assertTrue(service.listar(null, null).size() >= 55);
  }

  @Test
  void swaggerPublicaElCrudDeProductosYOcultaLoInterno() throws Exception {
    mvc.perform(get("/v3/api-docs"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.info.title").value("Clanky - catalogo"))
        .andExpect(jsonPath("$.paths['/api/productos'].get").exists())
        .andExpect(jsonPath("$.paths['/api/productos'].post").exists())
        .andExpect(jsonPath("$.paths['/api/productos/{id}'].put").exists())
        .andExpect(jsonPath("$.paths['/api/productos/{id}'].delete").exists())
        .andExpect(jsonPath("$.paths['/internal/ventas/{id}']").doesNotExist());
  }
}
