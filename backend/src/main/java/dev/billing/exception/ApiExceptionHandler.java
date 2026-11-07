package dev.billing.exception;

import dev.billing.dto.ApiDtos.ApiError;
import java.time.Instant;
import org.springframework.dao.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {
  private ResponseEntity<ApiError> error(int code, String message) {
    return ResponseEntity.status(code).body(new ApiError(code, message, Instant.now()));
  }

  @ExceptionHandler(ApiException.class)
  ResponseEntity<ApiError> api(ApiException e) {
    return error(e.status, e.getMessage());
  }

  @ExceptionHandler({
    ObjectOptimisticLockingFailureException.class,
    DataIntegrityViolationException.class
  })
  ResponseEntity<ApiError> conflict(Exception e) {
    return error(409, "Conflict: reload the dispute before trying again.");
  }

  @ExceptionHandler(AccessDeniedException.class)
  ResponseEntity<ApiError> denied() {
    return error(403, "Access denied.");
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ApiError> invalid(MethodArgumentNotValidException e) {
    return error(
        400,
        e.getBindingResult().getFieldErrors().stream()
            .map(x -> x.getField() + ": " + x.getDefaultMessage())
            .sorted()
            .findFirst()
            .orElse("Invalid request."));
  }

  @ExceptionHandler({
    HttpMessageNotReadableException.class,
    org.springframework.web.bind.MissingServletRequestParameterException.class,
    MethodArgumentTypeMismatchException.class,
    IllegalArgumentException.class
  })
  ResponseEntity<ApiError> bad(Exception e) {
    return error(400, "Invalid request value or format.");
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<ApiError> unexpected(Exception e) {
    org.slf4j.LoggerFactory.getLogger(ApiExceptionHandler.class)
        .error("Unexpected request failure", e);
    return error(
        500, "The request could not be completed. Please retry or contact the demo administrator.");
  }
}
