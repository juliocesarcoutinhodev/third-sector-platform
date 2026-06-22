package br.com.toponesystem.thirdsector.shared.adapter.in.web;

import br.com.toponesystem.thirdsector.shared.domain.exception.BusinessException;
import br.com.toponesystem.thirdsector.shared.domain.exception.ConflictException;
import br.com.toponesystem.thirdsector.shared.domain.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.regex.Pattern;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.of(403, "Forbidden", "Acesso negado."));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.of(401, "Unauthorized", "Autenticação necessária."));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        var errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> new ErrorResponse.FieldError(e.getField(), e.getDefaultMessage()))
                .toList();
        return ResponseEntity.badRequest()
                .body(ErrorResponse.ofValidation(errors));
    }

    private static final Pattern ENUM_PARSE_ERROR =
            Pattern.compile("from String \"([^\"]*)\": not one of the values accepted for (?:Enum class|Enum): \\[([^\\]]*)\\]");
    private static final Pattern FIELD_TYPE =
            Pattern.compile("type `[^`]*\\.([^`]+)`");

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMessageNotReadable(HttpMessageNotReadableException ex) {
        var msg = ex.getMessage();
        if (msg == null) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.of(400, "Bad Request", "Requisição com formato inválido."));
        }

        var matcher = ENUM_PARSE_ERROR.matcher(msg);
        if (matcher.find()) {
            var invalidValue = matcher.group(1);
            var allowedValues = matcher.group(2);
            var fieldMatcher = FIELD_TYPE.matcher(msg);
            var fieldName = fieldMatcher.find() ? fieldMatcher.group(1).toLowerCase() : "unknown";
            var message = String.format("Valor inválido '%s' para o campo '%s'. Valores aceitos: [%s].",
                    invalidValue, fieldName, allowedValues);
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.ofValidation(
                            java.util.List.of(new ErrorResponse.FieldError(fieldName, message))));
        }

        return ResponseEntity.badRequest()
                .body(ErrorResponse.of(400, "Bad Request", "Requisição com formato inválido."));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(404, "Not Found", ex.getMessage()));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(409, "Conflict", ex.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(409, "Conflict",
                        "Operação viola restrições de integridade dos dados."));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex) {
        return ResponseEntity.unprocessableEntity()
                .body(ErrorResponse.of(422, "Unprocessable Entity", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        log.error("Erro interno não tratado: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(500, "Internal Server Error", "Erro interno do servidor."));
    }
}
