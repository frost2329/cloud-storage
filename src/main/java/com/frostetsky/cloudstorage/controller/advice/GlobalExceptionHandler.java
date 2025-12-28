package com.frostetsky.cloudstorage.controller.advice;


import com.frostetsky.cloudstorage.dto.ErrorResponse;
import com.frostetsky.cloudstorage.excepiton.*;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserAlreadyExistException.class)
    public ResponseEntity<ErrorResponse> handleCreateUserException(UserAlreadyExistException e) {
        log.warn("User creation failed: username already exists: message={}", e.getMessage());
        return ResponseEntity.status(e.getStatusCode()).body(
                new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleCreateUserException(BadCredentialsException e) {
        log.warn("Authentication failed: bad credentials");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new ErrorResponse("Authentication failed: bad credentials"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        log.warn("Validation failed (request body): message={}", message);
        return ResponseEntity.badRequest().body(new ErrorResponse(message));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException e) {
        String errorMessage = e.getConstraintViolations().stream()
                .findFirst()
                .map(ConstraintViolation::getMessageTemplate)
                .orElse("Validation failed");
        log.warn("Validation failed (constraints): message={}", errorMessage);
        return ResponseEntity.badRequest().body(new ErrorResponse(errorMessage));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(ResourceNotFoundException e) {
        log.warn("Request failed: resource not found: message={}", e.getMessage());
        return ResponseEntity.status(e.getStatusCode()).body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(ResourceAlreadyExistException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(ResourceAlreadyExistException e) {
        log.warn("Request failed: resource already exists: message={}", e.getMessage());
        return ResponseEntity.status(e.getStatusCode()).body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(ResourceServiceException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(ResourceServiceException e) {
        log.error("Resource service error: {}", e.getMessage(), e);
        return ResponseEntity.status(e.getStatusCode()).body(new ErrorResponse("Error operation failed"));
    }

    @ExceptionHandler(MinioServiceException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MinioServiceException e) {
        log.error("Storage error: {}", e.getMessage(), e);
        return ResponseEntity.status(e.getStatusCode()).body(new ErrorResponse("Storage operation failed"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleOtherException(Exception e) {
        log.error("Unexpected server error: type={}, message={}", e.getClass().getSimpleName(), e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ErrorResponse("Unexpected server error"));
    }
}

