package cl.clanky.usuarios.controller;

import cl.clanky.common.exception.ApiException;
import cl.clanky.usuarios.dto.AuthResultado;
import cl.clanky.usuarios.dto.CambioPasswordRequest;
import cl.clanky.usuarios.dto.LoginRequest;
import cl.clanky.usuarios.dto.RegistroRequest;
import cl.clanky.usuarios.dto.SesionResponse;
import cl.clanky.usuarios.dto.UsuarioResponse;
import cl.clanky.usuarios.security.JwtKeys;
import cl.clanky.usuarios.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Autenticación", description = "Registro, login con JWT, renovación y cierre de sesión")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService service;
  private final JwtKeys keys;
  private final boolean secure;
  private final Set<String> origins;

  public AuthController(
      AuthService s,
      JwtKeys k,
      @Value("${app.cookie-secure}") boolean secure,
      @Value("${app.allowed-origins}") String origins,
      @Value("${app.gateway-url}") String gatewayUrl) {
    service = s;
    keys = k;
    this.secure = secure;
    Set<String> allowed = new HashSet<>(Arrays.asList(origins.split(",")));
    allowed.add(gatewayUrl);
    this.origins = Set.copyOf(allowed);
  }

  @io.swagger.v3.oas.annotations.Parameters({
    @io.swagger.v3.oas.annotations.Parameter(
        name = "X-Requested-With",
        in = io.swagger.v3.oas.annotations.enums.ParameterIn.HEADER,
        required = true,
        schema = @io.swagger.v3.oas.annotations.media.Schema(defaultValue = "Clanky"))
  })
  @Operation(summary = "Iniciar sesión (correo o usuario) y obtener el access token JWT")
  @PostMapping("/login")
  public SesionResponse login(
      @Valid @RequestBody LoginRequest form, HttpServletRequest req, HttpServletResponse res) {
    browserGuard(req);
    AuthResultado r = service.login(form.usuario(), form.contrasena());
    cookie(res, r.refresh(), 604800);
    return r.sesion();
  }

  @Operation(summary = "Registrar un cliente (el rol siempre es CLIENTE)")
  @PostMapping("/registro")
  @ResponseStatus(HttpStatus.CREATED)
  public UsuarioResponse registro(@Valid @RequestBody RegistroRequest r) {
    return service.registrar(r.usuario(), r.nombre(), r.email(), r.contrasena());
  }

  @io.swagger.v3.oas.annotations.Parameters({
    @io.swagger.v3.oas.annotations.Parameter(
        name = "X-Requested-With",
        in = io.swagger.v3.oas.annotations.enums.ParameterIn.HEADER,
        required = true,
        schema = @io.swagger.v3.oas.annotations.media.Schema(defaultValue = "Clanky"))
  })
  @Operation(summary = "Renovar el access token con la cookie HttpOnly (rota el refresh token)")
  @PostMapping("/refresh")
  public SesionResponse refresh(
      @CookieValue(value = "ck_refresh", required = false) String token,
      HttpServletRequest req,
      HttpServletResponse res) {
    browserGuard(req);
    AuthResultado r = service.refresh(token);
    cookie(res, r.refresh(), 604800);
    return r.sesion();
  }

  @io.swagger.v3.oas.annotations.Parameters({
    @io.swagger.v3.oas.annotations.Parameter(
        name = "X-Requested-With",
        in = io.swagger.v3.oas.annotations.enums.ParameterIn.HEADER,
        required = true,
        schema = @io.swagger.v3.oas.annotations.media.Schema(defaultValue = "Clanky"))
  })
  @Operation(summary = "Cerrar sesión y revocar el refresh token")
  @PostMapping("/logout")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void logout(
      @CookieValue(value = "ck_refresh", required = false) String token,
      HttpServletRequest req,
      HttpServletResponse res) {
    browserGuard(req);
    service.logout(token);
    cookie(res, "", 0);
  }

  @Operation(summary = "Datos del usuario autenticado")
  @GetMapping("/me")
  public UsuarioResponse me(@AuthenticationPrincipal Jwt jwt) {
    return AuthService.vista(service.usuario(Long.valueOf(jwt.getSubject())));
  }

  @Operation(summary = "Clave pública (JWKS) para verificar los tokens")
  @GetMapping("/jwks")
  public Map<String, Object> jwks() {
    return keys.publicJwks();
  }

  @Operation(summary = "Cambiar la contraseña (revoca todas las sesiones del usuario)")
  @PutMapping("/password")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void cambiarPassword(
      @Valid @RequestBody CambioPasswordRequest form,
      @AuthenticationPrincipal Jwt jwt,
      HttpServletResponse res) {
    service.cambiarPassword(Long.valueOf(jwt.getSubject()), form.actual(), form.nueva());
    cookie(res, "", 0);
  }

  private void browserGuard(HttpServletRequest req) {
    String origin = req.getHeader("Origin");
    if (!"Clanky".equals(req.getHeader("X-Requested-With"))
        || (origin != null && !origins.contains(origin)))
      throw new ApiException(403, "Origen o encabezado de sesión no permitido.");
  }

  private void cookie(HttpServletResponse res, String value, long seconds) {
    res.addHeader(
        HttpHeaders.SET_COOKIE,
        ResponseCookie.from("ck_refresh", value)
            .httpOnly(true)
            .secure(secure)
            .sameSite("Strict")
            .path("/api/auth")
            .maxAge(seconds)
            .build()
            .toString());
  }
}
