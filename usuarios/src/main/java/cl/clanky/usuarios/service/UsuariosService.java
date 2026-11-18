package cl.clanky.usuarios.service;

import cl.clanky.common.exception.ApiException;
import cl.clanky.usuarios.dto.*;
import cl.clanky.usuarios.dto.RegistroRequest;
import cl.clanky.usuarios.dto.UsuarioResponse;
import cl.clanky.usuarios.dto.UsuarioUpdateRequest;
import cl.clanky.usuarios.model.Usuario;
import cl.clanky.usuarios.repository.RefreshRepository;
import cl.clanky.usuarios.repository.UsuariosRepository;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuariosService {
  private final UsuariosRepository repo;
  private final RefreshRepository refresh;
  private final AuthService auth;

  public UsuariosService(UsuariosRepository r, RefreshRepository f, AuthService a) {
    repo = r;
    refresh = f;
    auth = a;
  }

  public List<UsuarioResponse> listar() {
    return repo.findAll().stream().filter(u -> u.activo).map(AuthService::vista).toList();
  }

  public UsuarioResponse obtener(Long id) {
    return AuthService.vista(buscar(id));
  }

  public UsuarioResponse crear(RegistroRequest r) {
    return auth.registrar(r.usuario(), r.nombre(), r.email(), r.contrasena());
  }

  @Transactional
  public UsuarioResponse editar(Long id, UsuarioUpdateRequest e) {
    Usuario u = buscar(id);
    if (u.rol.equals("ADMIN")
        && !e.rol().equals("ADMIN")
        && repo.countByRolAndActivoTrue("ADMIN") <= 1)
      throw new ApiException(409, "Debe existir al menos un administrador.");
    u.nombre = e.nombre().trim();
    u.email = e.email().trim().toLowerCase(Locale.ROOT);
    u.rol = e.rol();
    refresh.deleteByUsuarioId(id);
    return AuthService.vista(repo.save(u));
  }

  @Transactional
  public void eliminar(Long id, String actorId) {
    Usuario u = buscar(id);
    if (id.toString().equals(actorId))
      throw new ApiException(409, "No puedes desactivar tu propia cuenta.");
    if (u.rol.equals("ADMIN") && repo.countByRolAndActivoTrue("ADMIN") <= 1)
      throw new ApiException(409, "No puedes eliminar el último administrador.");
    u.activo = false;
    refresh.deleteByUsuarioId(id);
    repo.save(u);
  }

  private Usuario buscar(Long id) {
    return repo.findById(id)
        .filter(u -> u.activo)
        .orElseThrow(() -> new ApiException(404, "Usuario no encontrado."));
  }
}
