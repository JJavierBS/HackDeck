package com.cyberdeck.adapter.rest;

import com.cyberdeck.adapter.rest.dto.ApiErrorResponse;
import com.cyberdeck.adapter.rest.security.UnauthenticatedException;
import com.cyberdeck.application.exception.AccessDeniedException;
import com.cyberdeck.domain.exception.GameNotFoundException;
import com.cyberdeck.domain.exception.GameNotJoinableException;
import com.cyberdeck.domain.exception.InsufficientBudgetException;
import com.cyberdeck.domain.exception.MissingRequirementException;
import com.cyberdeck.domain.exception.UnknownCardException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(UnauthenticatedException.class)
    public ResponseEntity<ApiErrorResponse> handleUnauthenticated(UnauthenticatedException e) {
        return build(HttpStatus.UNAUTHORIZED, "no_autenticado", e.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException e) {
        return build(HttpStatus.FORBIDDEN, "acceso_denegado", e.getMessage());
    }

    @ExceptionHandler(GameNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleGameNotFound(GameNotFoundException e) {
        return build(HttpStatus.NOT_FOUND, "partida_no_encontrada", e.getMessage());
    }

    @ExceptionHandler(GameNotJoinableException.class)
    public ResponseEntity<ApiErrorResponse> handleGameNotJoinable(GameNotJoinableException e) {
        return build(HttpStatus.CONFLICT, "partida_no_disponible", e.getMessage());
    }

    @ExceptionHandler(InsufficientBudgetException.class)
    public ResponseEntity<ApiErrorResponse> handleInsufficientBudget(InsufficientBudgetException e) {
        return build(HttpStatus.CONFLICT, "presupuesto_insuficiente", e.getMessage());
    }

    @ExceptionHandler(MissingRequirementException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingRequirement(MissingRequirementException e) {
        return build(HttpStatus.CONFLICT, "falta_requisito", e.getMessage());
    }

    @ExceptionHandler(UnknownCardException.class)
    public ResponseEntity<ApiErrorResponse> handleUnknownCard(UnknownCardException e) {
        return build(HttpStatus.BAD_REQUEST, "carta_desconocida", e.getMessage());
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ApiErrorResponse> handleInvalidRequest(RuntimeException e) {
        return build(HttpStatus.BAD_REQUEST, "peticion_invalida", e.getMessage());
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<ApiErrorResponse> handleNotImplemented(UnsupportedOperationException e) {
        return build(HttpStatus.NOT_IMPLEMENTED, "no_implementado", e.getMessage());
    }

    private ResponseEntity<ApiErrorResponse> build(HttpStatus status, String error, String message) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(error, message));
    }
}
