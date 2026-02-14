package com.minierp.auth.api.error;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

public final class ApiErrorResponses {

    private ApiErrorResponses() {
    }

    public static ResponseEntity<ApiErrorResponse> response(
            HttpStatus status,
            String error,
            String message,
            HttpServletRequest request
    ) {
        return response(status, error, message, request, null);
    }

    public static ResponseEntity<ApiErrorResponse> response(
            HttpStatus status,
            String error,
            String message,
            HttpServletRequest request,
            List<ApiErrorResponse.FieldError> fieldErrors
    ) {
        return ResponseEntity.status(status)
                .body(body(status, error, message, request, fieldErrors));
    }

    public static ApiErrorResponse body(
            HttpStatus status,
            String error,
            String message,
            HttpServletRequest request,
            List<ApiErrorResponse.FieldError> fieldErrors
    ) {
        String correlationId = resolveCorrelationId(request);
        List<ApiErrorResponse.FieldError> safeFieldErrors =
                (fieldErrors == null || fieldErrors.isEmpty()) ? null : List.copyOf(fieldErrors);

        return new ApiErrorResponse(
                OffsetDateTime.now(ZoneOffset.UTC),
                status.value(),
                error,
                message,
                request.getRequestURI(),
                correlationId,
                safeFieldErrors
        );
    }

    private static String resolveCorrelationId(HttpServletRequest request) {
        String id = request.getHeader("X-Correlation-ID");
        if (id == null || id.isBlank()) {
            id = request.getHeader("X-Request-ID");
        }
        return (id == null || id.isBlank()) ? null : id;
    }
}
