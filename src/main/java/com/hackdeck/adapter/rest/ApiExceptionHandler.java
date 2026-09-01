package com.hackdeck.adapter.rest;

import com.hackdeck.adapter.rest.dto.ApiErrorResponse;
import com.hackdeck.adapter.rest.security.PayloadTooLargeException;
import com.hackdeck.adapter.rest.security.UnauthenticatedException;
import com.hackdeck.application.exception.AccessDeniedException;
import com.hackdeck.application.exception.CapacityExceededException;
import com.hackdeck.domain.exception.AlreadyDeployedException;
import com.hackdeck.domain.exception.GameNotFoundException;
import com.hackdeck.domain.exception.GameNotJoinableException;
import com.hackdeck.domain.exception.InsufficientBudgetException;
import com.hackdeck.domain.exception.MissingRequirementException;
import com.hackdeck.domain.exception.UnknownCardException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
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

    @ExceptionHandler(PayloadTooLargeException.class)
    public ResponseEntity<ApiErrorResponse> handlePayloadTooLarge(PayloadTooLargeException e) {
        return build(HttpStatus.PAYLOAD_TOO_LARGE, "cuerpo_demasiado_grande", e.getMessage());
    }

    /**
     * El cuerpo cortado por el tope salta a mitad del parseo, asi que puede
     * llegar envuelto en el error de JSON ilegible.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableBody(HttpMessageNotReadableException e) {
        for (Throwable causa = e.getCause(); causa != null; causa = causa.getCause()) {
            if (causa instanceof PayloadTooLargeException demasiado) {
                return handlePayloadTooLarge(demasiado);
            }
        }
        return build(HttpStatus.BAD_REQUEST, "peticion_invalida", "El cuerpo de la peticion no se puede leer");
    }

    @ExceptionHandler(CapacityExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleCapacityExceeded(CapacityExceededException e) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, "servidor_lleno", e.getMessage());
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

    @ExceptionHandler(AlreadyDeployedException.class)
    public ResponseEntity<ApiErrorResponse> handleAlreadyDeployed(AlreadyDeployedException e) {
        return build(HttpStatus.CONFLICT, "carta_ya_desplegada", e.getMessage());
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
