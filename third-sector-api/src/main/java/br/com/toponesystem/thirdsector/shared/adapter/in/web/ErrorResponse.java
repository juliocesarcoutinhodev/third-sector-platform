package br.com.toponesystem.thirdsector.shared.adapter.in.web;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        boolean success,
        int status,
        String error,
        String message,
        List<FieldError> errors,
        String timestamp
) {

    public record FieldError(String field, String message) {}

    public static ErrorResponse of(int status, String error, String message) {
        return new ErrorResponse(false, status, error, message, null,
                DateTimeFormatter.ISO_INSTANT.format(Instant.now()));
    }

    public static ErrorResponse ofValidation(List<FieldError> errors) {
        return new ErrorResponse(false, 400, "Bad Request", "Erro de validação.",
                errors, DateTimeFormatter.ISO_INSTANT.format(Instant.now()));
    }
}
