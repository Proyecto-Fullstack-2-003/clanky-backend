package cl.clanky.usuarios.dto;


import jakarta.validation.*;
import jakarta.validation.constraints.*;
import java.util.*;

public record AuthResultado(SesionResponse sesion, String refresh) {}
