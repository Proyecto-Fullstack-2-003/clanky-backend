package cl.clanky.usuarios.config;

import cl.clanky.usuarios.model.Usuario;
import cl.clanky.usuarios.repository.UsuariosRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class Seed {
  @Bean
  CommandLineRunner cargarDatos(
      UsuariosRepository r,
      BCryptPasswordEncoder encoder,
      @Value("${app.admin-password}") String admin,
      @Value("${app.cliente-password}") String cliente) {
    return args -> {
      for (String name : new String[] {"admin", "cliente"}) {
        if (!r.existsByUsuario(name)) {
          Usuario u = new Usuario();
          u.usuario = name;
          u.nombre = name.equals("admin") ? "Administrador Clanky" : "Cliente Clanky";
          u.email = name + "@clanky.local";
          u.rol = name.equals("admin") ? "ADMIN" : "CLIENTE";
          u.passwordHash = encoder.encode(name.equals("admin") ? admin : cliente);
          r.save(u);
        }
      }
    };
  }
}
