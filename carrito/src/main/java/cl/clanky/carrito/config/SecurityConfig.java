
package cl.clanky.carrito.config;

import cl.clanky.common.security.SecuritySupport;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.security.autoconfigure.web.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    JwtDecoder decoder(
            @Value("${app.jwks}") String jwks,
            @Value("${app.issuer}") String issuer) {

        return SecuritySupport.decoder(jwks, issuer);
    }

    // Permite acceder a la consola H2 solamente con el perfil h2.
    @Bean
    @Profile("h2")
    @Order(1)
    public SecurityFilterChain h2ConsoleSecurityFilterChain(
            HttpSecurity http) throws Exception {

        http
            .securityMatcher(PathRequest.toH2Console())
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll())
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }

    // Seguridad general de la API de Carrito.
    @Bean
    @Order(2)
    public SecurityFilterChain security(HttpSecurity http)
            throws Exception {

        SecuritySupport.base(http);

        http.authorizeHttpRequests(auth -> auth
            .requestMatchers(
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/actuator/health"
            ).permitAll()
            .requestMatchers("/api/v1/carrito/**")
            .authenticated()
            .anyRequest()
            .denyAll()
        );

        return http.build();
    }
}
