package cl.clanky.usuarios.config;

import cl.clanky.common.security.SecuritySupport;
import cl.clanky.usuarios.security.JwtKeys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
  @Bean
  BCryptPasswordEncoder passwords() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  JwtDecoder decoder(JwtKeys keys, @Value("${app.issuer}") String issuer) throws Exception {
    return keys.decoder(issuer);
  }

  @Bean
  SecurityFilterChain security(HttpSecurity h) throws Exception {
    SecuritySupport.base(h);
    h.authorizeHttpRequests(
        a ->
            a.requestMatchers(
                    "/api/auth/login",
                    "/api/auth/registro",
                    "/api/auth/refresh",
                    "/api/auth/logout",
                    "/api/auth/jwks",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/actuator/health")
                .permitAll()
                .requestMatchers("/api/usuarios/**")
                .hasRole("ADMIN")
                .anyRequest()
                .authenticated());
    return h.build();
  }
}
