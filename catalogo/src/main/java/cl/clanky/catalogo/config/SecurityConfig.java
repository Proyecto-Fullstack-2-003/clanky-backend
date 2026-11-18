package cl.clanky.catalogo.config;

import cl.clanky.common.security.SecuritySupport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
  @Bean
  JwtDecoder decoder(@Value("${app.jwks}") String jwks, @Value("${app.issuer}") String issuer) {
    return SecuritySupport.decoder(jwks, issuer);
  }

  @Bean
  SecurityFilterChain security(HttpSecurity h) throws Exception {
    SecuritySupport.base(h);
    h.authorizeHttpRequests(
        a ->
            a.requestMatchers(
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/actuator/health",
                    "/internal/ventas/**")
                .permitAll()
                .requestMatchers(HttpMethod.GET, "/api/productos/**")
                .permitAll()
                .requestMatchers("/api/productos/**")
                .hasRole("ADMIN")
                .anyRequest()
                .denyAll());
    return h.build();
  }
}
