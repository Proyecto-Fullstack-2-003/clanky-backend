package cl.clanky.usuarios.dto;

import jakarta.validation.*;
import jakarta.validation.constraints.*;
import java.util.*;

public record UsuarioResponse(Long id, String usuario, String nombre, String email, String rol) {}
