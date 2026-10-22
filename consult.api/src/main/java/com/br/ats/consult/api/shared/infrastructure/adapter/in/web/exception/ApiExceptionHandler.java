package com.br.ats.consult.api.shared.infrastructure.adapter.in.web.exception;

import com.br.ats.consult.api.candidate.domain.exception.CandidateEmailAlreadyExistsException;
import com.br.ats.consult.api.candidate.domain.exception.CandidateNotFoundException;
import com.br.ats.consult.api.client.domain.exception.ClientAlreadyExistsException;
import com.br.ats.consult.api.client.domain.exception.ClientNotFoundException;
import com.br.ats.consult.api.job.domain.exception.JobPositionNotFoundException;
import com.br.ats.consult.api.selectionprocess.domain.exception.CandidateAlreadyInProcessException;
import com.br.ats.consult.api.selectionprocess.domain.exception.CandidateApplicationNotFoundException;
import com.br.ats.consult.api.selectionprocess.domain.exception.SelectionProcessNotFoundException;
import com.br.ats.consult.api.selectionprocess.domain.exception.SelectionProcessNotOpenException;
import com.br.ats.consult.api.security.exception.EmailAlreadyRegisteredException;
import com.br.ats.consult.api.security.exception.TooManyRequestsException;
import com.br.ats.consult.api.shared.domain.exception.DomainException;
import com.br.ats.consult.api.shared.infrastructure.adapter.in.web.dto.ApiError;
import com.br.ats.consult.api.technology.domain.exception.TechnologyAlreadyExistsException;
import com.br.ats.consult.api.technology.domain.exception.TechnologyNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.stream.Stream;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler({JobPositionNotFoundException.class, TechnologyNotFoundException.class,
            ClientNotFoundException.class, CandidateNotFoundException.class,
            SelectionProcessNotFoundException.class, CandidateApplicationNotFoundException.class})
    public ResponseEntity<ApiError> handleNotFound(RuntimeException ex, HttpServletRequest request) {
        return response(HttpStatus.NOT_FOUND, request,
                ex.getMessage() != null ? ex.getMessage() : "Recurso não encontrado");
    }

    @ExceptionHandler({EmailAlreadyRegisteredException.class, TechnologyAlreadyExistsException.class,
            ClientAlreadyExistsException.class, CandidateEmailAlreadyExistsException.class,
            CandidateAlreadyInProcessException.class, SelectionProcessNotOpenException.class})
    public ResponseEntity<ApiError> handleConflict(DomainException ex, HttpServletRequest request) {
        return response(HttpStatus.CONFLICT, request, ex.getMessage());
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiError> handleOptimisticLock(ObjectOptimisticLockingFailureException ex,
                                                         HttpServletRequest request) {
        log.warn("Conflito de concorrência em {} {}: {}", request.getMethod(), request.getRequestURI(),
                ex.getMessage());
        return response(HttpStatus.CONFLICT, request,
                "O recurso foi alterado por outro utilizador. Recarregue os dados e tente novamente.");
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ApiError> handlePropertyReference(PropertyReferenceException ex,
                                                            HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, request, "Ordenação inválida.");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.warn("Violação de integridade em {} {}: {}", request.getMethod(), request.getRequestURI(),
                ex.getMostSpecificCause().getMessage());
        return response(HttpStatus.CONFLICT, request, "Os dados enviados entram em conflito com registos existentes.");
    }

    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<ApiError> handleTooManyRequests(TooManyRequestsException ex, HttpServletRequest request) {
        ApiError error = ApiError.of(request, HttpStatus.TOO_MANY_REQUESTS, ex.getMessage());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header(HttpHeaders.RETRY_AFTER, Long.toString(ex.getRetryAfterSeconds()))
                .header("X-Request-Id", error.traceId())
                .body(error);
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiError> handleDomain(DomainException ex, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, request, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleBadRequest(IllegalArgumentException ex, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, request,
                ex.getMessage() != null ? ex.getMessage() : "Requisição inválida");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthentication(AuthenticationException ex, HttpServletRequest request) {
        log.warn("Falha de autenticação em {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return response(HttpStatus.UNAUTHORIZED, request, "Credenciais inválidas");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Acesso negado em {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return response(HttpStatus.FORBIDDEN, request, "Acesso negado");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ApiError.FieldError> errors = Stream.concat(
                        ex.getBindingResult().getFieldErrors().stream()
                                .map(fieldError -> new ApiError.FieldError(
                                        fieldError.getField(), fieldError.getDefaultMessage())),
                        ex.getBindingResult().getGlobalErrors().stream()
                                .map(globalError -> new ApiError.FieldError(
                                        globalError.getObjectName(), globalError.getDefaultMessage())))
                .toList();

        String message = errors.isEmpty()
                ? "Dados inválidos"
                : errors.stream()
                .map(error -> error.field() + ": " + error.message())
                .reduce((first, second) -> first + ", " + second)
                .orElse("Dados inválidos");

        return response(HttpStatus.BAD_REQUEST, request, message, errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex,
                                                              HttpServletRequest request) {
        List<ApiError.FieldError> errors = ex.getConstraintViolations().stream()
                .map(violation -> new ApiError.FieldError(
                        violation.getPropertyPath().toString(), violation.getMessage()))
                .toList();
        return response(HttpStatus.BAD_REQUEST, request, "Parâmetros inválidos", errors);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiError> handleMethodValidation(HandlerMethodValidationException ex,
                                                           HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, request, "Parâmetros inválidos");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                       HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, request,
                "O parâmetro '" + ex.getName() + "' tem um valor inválido.");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingParameter(MissingServletRequestParameterException ex,
                                                           HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, request,
                "O parâmetro obrigatório '" + ex.getParameterName() + "' não foi informado.");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadableBody(HttpMessageNotReadableException ex,
                                                         HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, request, "O corpo da requisição é inválido ou está ausente.");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleNoResource(NoResourceFoundException ex, HttpServletRequest request) {
        return response(HttpStatus.NOT_FOUND, request, "Recurso não encontrado");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex,
                                                             HttpServletRequest request) {
        return response(HttpStatus.METHOD_NOT_ALLOWED, request, "Método HTTP não suportado para este recurso.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Erro interno não tratado em {} {}: {}", request.getMethod(), request.getRequestURI(),
                ex.getMessage(), ex);
        return response(HttpStatus.INTERNAL_SERVER_ERROR, request,
                "Ocorreu um erro interno no servidor. Por favor, tente novamente mais tarde.");
    }

    private ResponseEntity<ApiError> response(HttpStatus status, HttpServletRequest request, String message) {
        return response(status, request, message, List.of());
    }

    private ResponseEntity<ApiError> response(HttpStatus status, HttpServletRequest request, String message,
                                              List<ApiError.FieldError> errors) {
        ApiError error = ApiError.of(request, status, message, errors);
        return ResponseEntity.status(status)
                .header("X-Request-Id", error.traceId())
                .body(error);
    }
}
