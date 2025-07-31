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
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


@Slf4j
@ControllerAdvice
public class ExceptionHandlerController {

    @ExceptionHandler(UserAlreadyExistException.class)
    public ResponseEntity<ErrorResponse> handleCreateUserException(UserAlreadyExistException e) {
        log.error("Ошибка при создании пользователя: {}", e.getMessage(), e);
        return ResponseEntity.status(e.getStatusCode()).body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleCreateUserException(BadCredentialsException e) {
        log.error("Ошибка авторизации: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("Неверный логин или пароль"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        log.error("Ошибка валидации: {}", e.getMessage(), e);
        return ResponseEntity.badRequest().body(new ErrorResponse(message));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException e) {
        String errorMessage = e.getConstraintViolations().stream()
                .findFirst()
                .map(ConstraintViolation::getMessageTemplate)
                .orElse("Validation failed");
        log.error("Ошибка валидации: {}", errorMessage, e);
        return ResponseEntity.badRequest().body(new ErrorResponse(errorMessage));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(ResourceNotFoundException e) {
        log.error("Получена ошибка: {}", e.getMessage(), e);
        return ResponseEntity.status(e.getStatusCode()).body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(ResourceAlreadyExistException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(ResourceAlreadyExistException e) {
        log.error("Получена ошибка: {}", e.getMessage(), e);
        return ResponseEntity.status(e.getStatusCode()).body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(DirectoryServiceException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(DirectoryServiceException e) {
        log.error("Ошибка при с файлами DirectoryService: {}", e.getMessage(), e);
        return ResponseEntity.status(e.getStatusCode()).body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleOtherException(BaseException e) {
        log.error("Получена непредвиденная ошибка {}", e.getMessage(), e);
        return ResponseEntity.status(e.getStatusCode()).body(
                new ErrorResponse("Произошла непредвиденная ошибка: %s".formatted(e.getMessage())));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleOtherException(Exception e) {
        log.error("Получена непредвиденная ошибка {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ErrorResponse("Произошла непредвиденная ошибка: %s".formatted(e.getMessage())));
    }
}
