package cl.clanky.common.security;


import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.*;

public final class SecuritySupport {
  /** Audiencia que debe llevar todo access token emitido para la API de Clanky. */
  public static final String AUDIENCE = "clanky-api";

  public static OAuth2TokenValidator<Jwt> audience() {
    return jwt ->
        jwt.getAudience() != null && jwt.getAudience().contains(AUDIENCE)
            ? OAuth2TokenValidatorResult.success()
            : OAuth2TokenValidatorResult.failure(
                new OAuth2Error("invalid_token", "Audiencia incorrecta", null));
  }

  /** Firma, vigencia, emisor y audiencia: los cuatro controles que exige cada servicio. */
  public static OAuth2TokenValidator<Jwt> validator(String issuer) {
    return new DelegatingOAuth2TokenValidator<>(
        JwtValidators.createDefaultWithIssuer(issuer), audience());
  }

  public static JwtDecoder decoder(String jwks, String issuer) {
    NimbusJwtDecoder d = NimbusJwtDecoder.withJwkSetUri(jwks).build();
    d.setJwtValidator(validator(issuer));
    return d;
  }

  public static JwtAuthenticationConverter converter() {
    JwtGrantedAuthoritiesConverter a = new JwtGrantedAuthoritiesConverter();
    a.setAuthoritiesClaimName("roles");
    a.setAuthorityPrefix("ROLE_");
    JwtAuthenticationConverter c = new JwtAuthenticationConverter();
    c.setJwtGrantedAuthoritiesConverter(a);
    return c;
  }

  public static void base(HttpSecurity h) throws Exception {
    h.csrf(c -> c.disable())
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .oauth2ResourceServer(
            o ->
                o.jwt(j -> j.jwtAuthenticationConverter(converter()))
                    .authenticationEntryPoint(
                        (req, res, e) -> {
                          res.setStatus(401);
                          res.setContentType("application/json");
                          res.setCharacterEncoding("UTF-8");
                          res.getWriter()
                              .write(
                                  "{\"status\":401,\"mensaje\":\"Debes iniciar sesión o renovar tu"
                                      + " sesión.\"}");
                        }))
        .exceptionHandling(
            e ->
                e.accessDeniedHandler(
                    (req, res, ex) -> {
                      res.setStatus(403);
                      res.setContentType("application/json");
                      res.setCharacterEncoding("UTF-8");
                      res.getWriter()
                          .write(
                              "{\"status\":403,\"mensaje\":\"No tienes permisos para esta"
                                  + " acción.\"}");
                    }));
  }
}
