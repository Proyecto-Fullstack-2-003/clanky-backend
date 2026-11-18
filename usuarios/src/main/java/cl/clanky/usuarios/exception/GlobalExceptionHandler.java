package cl.clanky.usuarios.exception;

import cl.clanky.common.exception.ApiErrors;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @ControllerAdvice aplica los @ExceptionHandler heredados a todos los controladores.
 */
@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandler extends ApiErrors {}
