package cl.clanky.usuarios.service;

import cl.clanky.common.exception.ApiException;
import cl.clanky.common.security.SecuritySupport;
import cl.clanky.usuarios.dto.AuthResultado;
import cl.clanky.usuarios.dto.SesionResponse;
import cl.clanky.usuarios.dto.UsuarioResponse;
import cl.clanky.usuarios.model.RefreshSession;
import cl.clanky.usuarios.model.Usuario;
import cl.clanky.usuarios.repository.RefreshRepository;
import cl.clanky.usuarios.repository.UsuariosRepository;
import cl.clanky.usuarios.security.JwtKeys;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

  private static final String CREDENCIALES_INVALIDAS = "Usuario o contraseña incorrectos.";
  private static final int MAX_INTENTOS = 5;
  private static final Duration BLOQUEO = Duration.ofMinutes(5);

  private final UsuariosRepository users;
  private final RefreshRepository sessions;
  private final BCryptPasswordEncoder passwords;
  private final JwtEncoder encoder;
  private final String issuer;
  private final long seconds;

  public AuthService(
      UsuariosRepository u,
      RefreshRepository s,
      BCryptPasswordEncoder p,
      JwtKeys keys,
      @Value("${app.issuer}") String issuer,
      @Value("${app.access-seconds}") long seconds) {
    users = u;
    sessions = s;
    passwords = p;
    encoder = keys.encoder();
    this.issuer = issuer;
    this.seconds = seconds;
  }

  public static UsuarioResponse vista(Usuario u) {
    return new UsuarioResponse(u.id, u.usuario, u.nombre, u.email, u.rol);
  }

  public Usuario usuario(Long id) {
    return users
        .findById(id)
        .filter(u -> u.activo)
        .orElseThrow(() -> new ApiException(401, "La cuenta no está activa."));
  }

  // noRollbackFor: el contador de intentos fallidos debe guardarse aunque el login termine en error.
  @Transactional(noRollbackFor = ApiException.class)
  public AuthResultado login(String user, String pass) {
    // El cliente ingresa con su correo; el administrador, con su nombre de usuario.
    String id = user.trim().toLowerCase(Locale.ROOT);
    Usuario u =
        (id.contains("@") ? users.findByEmail(id) : users.findByUsuario(id))
            .filter(x -> x.activo)
            .orElseThrow(() -> new ApiException(401, CREDENCIALES_INVALIDAS));
    if (u.bloqueadoHasta != null && u.bloqueadoHasta.isAfter(Instant.now()))
      throw new ApiException(
          429, "Cuenta bloqueada temporalmente por intentos fallidos. Intenta en unos minutos.");
    if (!passwords.matches(pass, u.passwordHash)) {
      int intentos = (u.intentosFallidos == null ? 0 : u.intentosFallidos) + 1;
      if (intentos >= MAX_INTENTOS) {
        u.bloqueadoHasta = Instant.now().plus(BLOQUEO);
        intentos = 0;
      }
      u.intentosFallidos = intentos;
      users.save(u);
      throw new ApiException(401, CREDENCIALES_INVALIDAS);
    }
    u.intentosFallidos = 0;
    u.bloqueadoHasta = null;
    users.save(u);
    return emitir(u);
  }

  @Transactional
  public AuthResultado refresh(String token) {
    RefreshSession s =
        sessions
            .bloquear(hash(token))
            .filter(x -> x.expira.isAfter(Instant.now()))
            .orElseThrow(
                () -> new ApiException(401, "La sesión expiró. Inicia sesión nuevamente."));
    Usuario u = usuario(s.usuarioId);
    sessions.delete(s);
    sessions.flush();
    return emitir(u);
  }

  @Transactional
  public void logout(String token) {
    if (token != null) sessions.deleteById(hash(token));
  }

  private AuthResultado emitir(Usuario u) {
    Instant now = Instant.now();
    sessions.deleteByExpiraBefore(now);
    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .issuer(issuer)
            .subject(u.id.toString())
            .audience(List.of(SecuritySupport.AUDIENCE))
            .id(UUID.randomUUID().toString())
            .issuedAt(now)
            .expiresAt(now.plusSeconds(seconds))
            .claim("roles", List.of(u.rol))
            .claim("usuario", u.usuario)
            .build();
    String access =
        encoder
            .encode(
                JwtEncoderParameters.from(JwsHeader.with(SignatureAlgorithm.RS256).build(), claims))
            .getTokenValue();
    byte[] bytes = new byte[48];
    new SecureRandom().nextBytes(bytes);
    String refresh = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    sessions.save(new RefreshSession(hash(refresh), u.id, now.plus(Duration.ofDays(7))));
    return new AuthResultado(new SesionResponse(access, seconds, vista(u)), refresh);
  }

  static String hash(String value) {
    if (value == null || value.isBlank())
      throw new ApiException(401, "No existe una sesión activa.");
    try {
      return HexFormat.of()
          .formatHex(
              MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }

  @Transactional
  public UsuarioResponse registrar(String user, String nombre, String email, String pass) {
    String username = user.trim().toLowerCase(Locale.ROOT),
        mail = email.trim().toLowerCase(Locale.ROOT);
    if (users.existsByUsuario(username) || users.existsByEmail(mail))
      throw new ApiException(409, "El usuario o correo ya está registrado.");
    Usuario u = new Usuario();
    u.usuario = username;
    u.nombre = nombre.trim();
    u.email = mail;
    u.passwordHash = passwords.encode(pass);
    return vista(users.save(u));
  }

  /** Cambia la contraseña y revoca todas las sesiones de renovación del usuario. */
  @Transactional
  public void cambiarPassword(Long id, String actual, String nueva) {
    Usuario u = usuario(id);
    if (!passwords.matches(actual, u.passwordHash))
      throw new ApiException(400, "La contraseña actual no es correcta.");
    if (passwords.matches(nueva, u.passwordHash))
      throw new ApiException(400, "La nueva contraseña debe ser distinta de la actual.");
    u.passwordHash = passwords.encode(nueva);
    users.save(u);
    sessions.deleteByUsuarioId(id);
  }
}
