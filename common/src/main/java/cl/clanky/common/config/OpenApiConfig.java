package cl.clanky.common.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.*;
import io.swagger.v3.oas.models.security.*;
import io.swagger.v3.oas.models.servers.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;

@Configuration
public class OpenApiConfig {
  @Bean
  OpenAPI api(
      @Value("${spring.application.name}") String name,
      @Value("${app.swagger-url:${app.gateway-url:http://localhost:8080}}") String serverUrl) {
    return new OpenAPI()
        .info(
            new Info()
                .title("Clanky - " + name)
                .version("1.0.0")
                .description(
                    "API de Clanky Disquería. Para probar endpoints protegidos: 1) POST"
                        + " /api/auth/login, 2) copia accessToken, 3) botón Authorize y pega el"
                        + " token (sin la palabra Bearer)."))
        .addServersItem(new Server().url(serverUrl))
        .components(
            new Components()
                .addSecuritySchemes(
                    "bearerAuth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
        .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
  }
}
