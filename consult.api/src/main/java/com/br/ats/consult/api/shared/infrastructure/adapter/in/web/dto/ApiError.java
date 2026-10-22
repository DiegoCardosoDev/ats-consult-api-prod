package com.br.ats.consult.api.shared.infrastructure.adapter.in.web.dto;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldError> errors,
        String traceId
) {

    public static ApiError of(HttpServletRequest request, HttpStatus status, String message) {
        return of(request, status, message, List.of());
    }

    public static ApiError of(HttpServletRequest request, HttpStatus status, String message, List<FieldError> errors) {
        return new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                errors == null ? List.of() : List.copyOf(errors),
                traceId(request)
        );
    }

    private static String traceId(HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-Id");
        return requestId == null || requestId.isBlank() ? UUID.randomUUID().toString() : requestId;
    }

    public record FieldError(String field, String message) {
    }
}
