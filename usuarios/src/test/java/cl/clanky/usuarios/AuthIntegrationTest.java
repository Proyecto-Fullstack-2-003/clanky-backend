package cl.clanky.usuarios;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import cl.clanky.usuarios.repository.UsuariosRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@org.springframework.test.context.ActiveProfiles("h2")
@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:auth-test",
      "app.keys-directory=./target/test-keys"
    })
@AutoConfigureMockMvc
class AuthIntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired UsuariosRepository users;
  private static org.springframework.test.web.servlet.request.RequestPostProcessor adminJwt() {
    return org.springframework.security.test.web.servlet.request
        .SecurityMockMvcRequestPostProcessors.jwt()
        .jwt(j -> j.subject("1"))
        .authorities(
            new org.springframework.security.core.authority.SimpleGrantedAuthority(
                "ROLE_ADMIN"
            )
        );
}

  @Test
  void loginCorrectoEmiteJwtYCookieHttpOnly() throws Exception {
    mvc.perform(
            post("/api/auth/login")
                .header("X-Requested-With", "Clanky")
                .contentType("application/json")
                .content("{\"usuario\":\"admin\",\"contrasena\":\"Admin1234!\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").isNotEmpty())
        .andExpect(jsonPath("$.usuario.rol").value("ADMIN"))
        .andExpect(cookie().httpOnly("ck_refresh", true));
  }

  @Test
  void loginIncorrectoDevuelve401() throws Exception {
    mvc.perform(
            post("/api/auth/login")
                .header("X-Requested-With", "Clanky")
                .contentType("application/json")
                .content("{\"usuario\":\"admin\",\"contrasena\":\"incorrecta\"}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void refreshSinCookieDevuelve401() throws Exception {
    mvc.perform(post("/api/auth/refresh").header("X-Requested-With", "Clanky"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void sesionRechazaOrigenAjeno() throws Exception {
    mvc.perform(
            post("/api/auth/login")
                .header("X-Requested-With", "Clanky")
                .header("Origin", "https://otro.example")
                .contentType("application/json")
                .content("{\"usuario\":\"admin\",\"contrasena\":\"Admin1234!\"}"))
        .andExpect(status().isForbidden());
  }

  @Test
  void registroNoPermiteElegirRol() throws Exception {
    mvc.perform(
            post("/api/auth/registro")
                .contentType("application/json")
                .content(
                    "{\"usuario\":\"nuevo-test\",\"nombre\":\"Nuevo"
                        + " Usuario\",\"email\":\"nuevo@test.cl\",\"contrasena\":\"Cliente1234!\",\"rol\":\"ADMIN\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.rol").value("CLIENTE"));
  }

  @Test
  void passwordNoSeGuardaEnTextoPlano() {
    assertTrue(users.findByUsuario("admin").orElseThrow().passwordHash.startsWith("$2"));
  }

  @Test
  void usuariosSinTokenDevuelve401() throws Exception {
    mvc.perform(get("/api/usuarios")).andExpect(status().isUnauthorized());
  }

  @Test
  void loginDesdeSwaggerDelGateway() throws Exception {
    mvc.perform(
            post("/api/auth/login")
                .header("X-Requested-With", "Clanky")
                .header("Origin", "http://localhost:8080")
                .contentType("application/json")
                .content("{\"usuario\":\"admin\",\"contrasena\":\"Admin1234!\"}"))
        .andExpect(status().isOk());
  }

@Test
void crudUsuariosRecorreControllerServiceRepositoryYJpa() throws Exception {
    String username = "crud-" + java.util.UUID.randomUUID()
        .toString().substring(0, 8);

    String created =
        mvc.perform(
                post("/api/usuarios")
                    .with(adminJwt())
                    .contentType("application/json")
                    .content(
                        "{\"usuario\":\"" + username
                            + "\",\"nombre\":\"Alumno Prueba\",\"email\":\""
                            + username
                            + "@test.cl\",\"contrasena\":\"Alumno12345!\"}"
                    )
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.passwordHash").doesNotExist())
            .andReturn()
            .getResponse()
            .getContentAsString();

    long id = Long.parseLong(
        created.replaceAll("(?s).*\"id\":(\\d+).*", "$1")
    );

    mvc.perform(get("/api/usuarios/" + id).with(adminJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.usuario").value(username));

    mvc.perform(
            put("/api/usuarios/" + id)
                .with(adminJwt())
                .contentType("application/json")
                .content(
                    "{\"nombre\":\"Alumno Editado\",\"email\":\""
                        + username
                        + "@test.cl\",\"rol\":\"CLIENTE\"}"
                )
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nombre").value("Alumno Editado"));

    mvc.perform(delete("/api/usuarios/" + id).with(adminJwt()))
        .andExpect(status().isNoContent());

    mvc.perform(get("/api/usuarios/" + id).with(adminJwt()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.mensaje").value("Usuario no encontrado."));

    assertFalse(users.findById(id).orElseThrow().activo);
}


  
@Test
void adviceConvierteUsuarioInexistenteEn404() throws Exception {
    mvc.perform(get("/api/usuarios/9999999").with(adminJwt()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404));
}

@Test
void noSePuedeDegradarAlUltimoAdministrador() throws Exception {
    long id = users.findByUsuario("admin").orElseThrow().id;

    mvc.perform(
            put("/api/usuarios/" + id)
                .with(adminJwt())
                .contentType("application/json")
                .content(
                    "{\"nombre\":\"Administrador\",\"email\":\"admin@clanky.local\",\"rol\":\"CLIENTE\"}"
                )
        )
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.status").value(409));

    assertEquals("ADMIN", users.findById(id).orElseThrow().rol);
}

  @Test
  void noSePuedeDesactivarLaPropiaCuenta() throws Exception {
    long id = users.findByUsuario("admin").orElseThrow().id;
    mvc.perform(
            delete("/api/usuarios/" + id)
                .with(
                    org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt(j -> j.subject(Long.toString(id)))
                        .authorities(
                            new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                "ROLE_ADMIN"))))
        .andExpect(status().isConflict());
    assertTrue(users.findById(id).orElseThrow().activo);
  }

  // ---------- Revisión de la autenticación por tokens ----------

  private static String body(String usuario, String contrasena) {
    return "{\"usuario\":\"" + usuario + "\",\"contrasena\":\"" + contrasena + "\"}";
  }

  private MvcResult login(String usuario, String contrasena) throws Exception {
    return mvc.perform(
            post("/api/auth/login")
                .header("X-Requested-With", "Clanky")
                .contentType("application/json")
                .content(body(usuario, contrasena)))
        .andReturn();
  }

  private static String accessToken(MvcResult r) throws Exception {
    return r.getResponse()
        .getContentAsString()
        .replaceAll("(?s).*\"accessToken\":\"([^\"]+)\".*", "$1");
  }

  private static String decodificar(String parte) {
    return new String(java.util.Base64.getUrlDecoder().decode(parte), java.nio.charset.StandardCharsets.UTF_8);
  }

  private String registrar(String prefijo, String clave) throws Exception {
    String nombre = prefijo + java.util.UUID.randomUUID().toString().substring(0, 8);
    mvc.perform(
            post("/api/auth/registro")
                .contentType("application/json")
                .content(
                    "{\"usuario\":\"" + nombre + "\",\"nombre\":\"Prueba Token\",\"email\":\""
                        + nombre + "@test.cl\",\"contrasena\":\"" + clave + "\"}"))
        .andExpect(status().isCreated());
    return nombre;
  }

  @Test
void elAccessTokenEstaFirmadoConRs256YLlevaRolesAudienciaYJti() throws Exception {
    String[] partes = accessToken(login("admin", "Admin1234!")).split("\\.");

    assertEquals(3, partes.length);
    assertTrue(decodificar(partes[0]).contains("\"alg\":\"RS256\""));

    String claims = decodificar(partes[1]);

    assertTrue(claims.contains("\"roles\":[\"ADMIN\"]"));
    assertTrue(
        claims.contains("\"aud\":\"clanky-api\"")
        || claims.contains("\"aud\":[\"clanky-api\"]")
    );
    assertTrue(claims.contains("\"jti\""));
    assertTrue(claims.contains("\"exp\""));
    assertFalse(claims.contains("passwordHash"));
}

  @Test
  void conTokenValidoSeAccedeYConTokenAlteradoOSinTokenNo() throws Exception {
    String token = accessToken(login("admin", "Admin1234!"));
    mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.usuario").value("admin"));
    String alterado = token.substring(0, token.length() - 4) + (token.endsWith("AAAA") ? "BBBB" : "AAAA");
    mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + alterado))
        .andExpect(status().isUnauthorized());
    mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
  }

  @Test
  void tokenDeClienteNoPuedeUsarRecursosDeAdministrador() throws Exception {
    String token = accessToken(login("cliente", "Cliente1234!"));
    mvc.perform(get("/api/usuarios").header("Authorization", "Bearer " + token))
        .andExpect(status().isForbidden());
    mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk());
  }

  @Test
  void refreshRotaElTokenYElAnteriorDejaDeServir() throws Exception {
    jakarta.servlet.http.Cookie primera = login("cliente", "Cliente1234!").getResponse().getCookie("ck_refresh");
    assertNotNull(primera);
    MvcResult renovada =
        mvc.perform(post("/api/auth/refresh").header("X-Requested-With", "Clanky").cookie(primera))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").isNotEmpty())
            .andReturn();
    assertNotEquals(primera.getValue(), renovada.getResponse().getCookie("ck_refresh").getValue());
    mvc.perform(post("/api/auth/refresh").header("X-Requested-With", "Clanky").cookie(primera))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void logoutRevocaElRefreshToken() throws Exception {
    jakarta.servlet.http.Cookie cookie = login("cliente", "Cliente1234!").getResponse().getCookie("ck_refresh");
    mvc.perform(post("/api/auth/logout").header("X-Requested-With", "Clanky").cookie(cookie))
        .andExpect(status().isNoContent());
    mvc.perform(post("/api/auth/refresh").header("X-Requested-With", "Clanky").cookie(cookie))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void clienteEntraConSuCorreo() throws Exception {
    mvc.perform(
            post("/api/auth/login")
                .header("X-Requested-With", "Clanky")
                .contentType("application/json")
                .content(body("cliente@clanky.local", "Cliente1234!")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.usuario.rol").value("CLIENTE"));
  }

  @Test
  void tras5IntentosFallidosLaCuentaSeBloquea() throws Exception {
    String nombre = registrar("bloqueo-", "Cliente1234!");
    for (int i = 0; i < 5; i++)
      assertEquals(401, login(nombre, "incorrecta").getResponse().getStatus());
    assertEquals(429, login(nombre, "Cliente1234!").getResponse().getStatus());
  }

  @Test
  void cambiarPasswordExigeLaActualYRevocaLasSesiones() throws Exception {
    String nombre = registrar("clave-", "Cliente1234!");
    MvcResult sesion = login(nombre, "Cliente1234!");
    String token = accessToken(sesion);
    jakarta.servlet.http.Cookie refresh = sesion.getResponse().getCookie("ck_refresh");

    mvc.perform(
            put("/api/auth/password")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content("{\"actual\":\"equivocada\",\"nueva\":\"Nueva12345!\"}"))
        .andExpect(status().isBadRequest());
    mvc.perform(
            put("/api/auth/password")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content("{\"actual\":\"Cliente1234!\",\"nueva\":\"debil\"}"))
        .andExpect(status().isBadRequest());
    mvc.perform(
            put("/api/auth/password")
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content("{\"actual\":\"Cliente1234!\",\"nueva\":\"Nueva12345!\"}"))
        .andExpect(status().isNoContent());

    mvc.perform(post("/api/auth/refresh").header("X-Requested-With", "Clanky").cookie(refresh))
        .andExpect(status().isUnauthorized());
    assertEquals(401, login(nombre, "Cliente1234!").getResponse().getStatus());
    assertEquals(200, login(nombre, "Nueva12345!").getResponse().getStatus());
  }

  @Test
  void registroRechazaContrasenasDebiles() throws Exception {
    mvc.perform(
            post("/api/auth/registro")
                .contentType("application/json")
                .content(
                    "{\"usuario\":\"debil-test\",\"nombre\":\"Debil\",\"email\":\"debil@test.cl\",\"contrasena\":\"password\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.campos.contrasena").exists());
  }

  // ---------- Swagger ----------

  @Test
  void swaggerPublicaAutenticacionYCrudDeUsuarios() throws Exception {
    mvc.perform(get("/v3/api-docs"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.info.title").value("Clanky - usuarios"))
        .andExpect(jsonPath("$.paths['/api/auth/login'].post").exists())
        .andExpect(jsonPath("$.paths['/api/auth/registro'].post").exists())
        .andExpect(jsonPath("$.paths['/api/auth/refresh'].post").exists())
        .andExpect(jsonPath("$.paths['/api/auth/password'].put").exists())
        .andExpect(jsonPath("$.paths['/api/usuarios'].get").exists())
        .andExpect(jsonPath("$.paths['/api/usuarios'].post").exists())
        .andExpect(jsonPath("$.paths['/api/usuarios/{id}'].put").exists())
        .andExpect(jsonPath("$.paths['/api/usuarios/{id}'].delete").exists())
        .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
        .andExpect(
            jsonPath("$.paths['/api/auth/login'].post.parameters[?(@.name=='X-Requested-With')]")
                .exists());
  }

}
