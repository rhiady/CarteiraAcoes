package com.carteiraacoesbackend.exceptions;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import com.carteiraacoesbackend.dto.ErroResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErroResponse> handleApi(ApiException exception, HttpServletRequest request) {
        return response(exception.getStatus(), exception.getCode(), exception.getMessage(), request);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    public ResponseEntity<ErroResponse> handleValidation(Exception exception, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Dados da requisição são inválidos.", request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResponse> handleIntegrity(DataIntegrityViolationException exception,
                                                         HttpServletRequest request) {
        return response(HttpStatus.CONFLICT, "DATA_INTEGRITY_VIOLATION", "Os dados informados violam uma regra de integridade.", request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException exception,
                                                                 HttpServletRequest request) {
        return response(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", "Método HTTP não permitido para este recurso.", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> handleUnexpected(Exception exception, HttpServletRequest request) {
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Ocorreu um erro inesperado.", request);
    }

    private ResponseEntity<ErroResponse> response(HttpStatus status, String code, String message,
                                                   HttpServletRequest request) {
        ErroResponse body = new ErroResponse(OffsetDateTime.now(ZoneOffset.UTC), status.value(), code, message,
                request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
