package com.minierp.auth.api.error;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        String correlationId,
        List<FieldError> fieldErrors
) {
    public record FieldError(String field, String message) {}
}
