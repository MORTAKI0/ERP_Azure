package com.minierp.auth.api.error;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ApiErrorResponse> invalidRefresh(HttpServletRequest request) {
        return ApiErrorResponses.response(
                HttpStatus.UNAUTHORIZED,
                "invalid_refresh_token",
                "Invalid refresh token",
                request
        );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> badCreds(HttpServletRequest request) {
        return ApiErrorResponses.response(
                HttpStatus.UNAUTHORIZED,
                "invalid_credentials",
                "Invalid credentials",
                request
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> invalidBody(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        List<ApiErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toFieldError)
                .toList();

        return ApiErrorResponses.response(
                HttpStatus.BAD_REQUEST,
                "validation_failed",
                "Validation failed",
                request,
                fieldErrors
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> constraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        List<ApiErrorResponse.FieldError> fieldErrors = ex.getConstraintViolations()
                .stream()
                .map(violation -> new ApiErrorResponse.FieldError(
                        violation.getPropertyPath().toString(),
                        violation.getMessage()
                ))
                .toList();

        return ApiErrorResponses.response(
                HttpStatus.BAD_REQUEST,
                "validation_failed",
                "Validation failed",
                request,
                fieldErrors
        );
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiErrorResponse> badRequest(Exception ex, HttpServletRequest request) {
        return ApiErrorResponses.response(
                HttpStatus.BAD_REQUEST,
                "validation_failed",
                "Validation failed",
                request
        );
    }

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class, EntityNotFoundException.class, NotFoundException.class})
    public ResponseEntity<ApiErrorResponse> notFound(Exception ex, HttpServletRequest request) {
        return ApiErrorResponses.response(
                HttpStatus.NOT_FOUND,
                "not_found",
                messageOrDefault(ex.getMessage(), "Resource not found"),
                request
        );
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiErrorResponse> conflict(ConflictException ex, HttpServletRequest request) {
        return ApiErrorResponses.response(
                HttpStatus.CONFLICT,
                "conflict",
                messageOrDefault(ex.getMessage(), "Conflict"),
                request
        );
    }

    @ExceptionHandler({AuthenticationException.class, AccessDeniedException.class})
    public ResponseEntity<ApiErrorResponse> authErrors(Exception ex, HttpServletRequest request) {
        HttpStatus status = ex instanceof AccessDeniedException ? HttpStatus.FORBIDDEN : HttpStatus.UNAUTHORIZED;
        String error = ex instanceof AccessDeniedException ? "forbidden" : "unauthorized";
        String message = ex instanceof AccessDeniedException ? "Access is denied" : "Authentication is required";

        return ApiErrorResponses.response(status, error, message, request);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> responseStatus(
            ResponseStatusException ex,
            HttpServletRequest request
    ) {
        int rawStatus = ex.getStatusCode().value();
        HttpStatus status = HttpStatus.resolve(rawStatus);
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return ApiErrorResponses.response(
                status,
                mapErrorCode(status),
                messageOrDefault(ex.getReason(), status.getReasonPhrase()),
                request
        );
    }

    private ApiErrorResponse.FieldError toFieldError(FieldError fieldError) {
        String message = fieldError.getDefaultMessage();
        if (message == null || message.isBlank()) {
            message = "Invalid value";
        }
        return new ApiErrorResponse.FieldError(fieldError.getField(), message);
    }

    private String messageOrDefault(String message, String defaultValue) {
        return (message == null || message.isBlank()) ? defaultValue : message;
    }

    private String mapErrorCode(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> "bad_request";
            case UNAUTHORIZED -> "unauthorized";
            case FORBIDDEN -> "forbidden";
            case NOT_FOUND -> "not_found";
            case CONFLICT -> "conflict";
            default -> status.is4xxClientError() ? "bad_request" : "internal_server_error";
        };
    }
}
