package cl.clanky.usuarios.controller;

import cl.clanky.usuarios.dto.*;
import cl.clanky.usuarios.dto.RegistroRequest;
import cl.clanky.usuarios.dto.UsuarioResponse;
import cl.clanky.usuarios.dto.UsuarioUpdateRequest;
import cl.clanky.usuarios.service.UsuariosService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Usuarios", description = "CRUD de usuarios (solo ADMIN)")
@RestController
@RequestMapping("/api/usuarios")
public class UsuariosController {
  private final UsuariosService service;

  public UsuariosController(UsuariosService service) {
    this.service = service;
  }

  @Operation(summary = "Listar usuarios activos")
  @GetMapping
  public List<UsuarioResponse> listar() {
    return service.listar();
  }

  @Operation(summary = "Obtener un usuario por id")
  @GetMapping("/{id}")
  public UsuarioResponse obtener(@PathVariable Long id) {
    return service.obtener(id);
  }

  @Operation(summary = "Crear un usuario (rol CLIENTE)")
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public UsuarioResponse crear(@Valid @RequestBody RegistroRequest request) {
    return service.crear(request);
  }

  @Operation(summary = "Editar nombre, correo y rol")
  @PutMapping("/{id}")
  public UsuarioResponse editar(
      @PathVariable Long id, @Valid @RequestBody UsuarioUpdateRequest request) {
    return service.editar(id, request);
  }

  @Operation(summary = "Desactivar un usuario (baja lógica)")
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void eliminar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
    service.eliminar(id, jwt.getSubject());
  }
}
