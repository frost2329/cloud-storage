package com.frostetsky.cloudstorage.controller;


import com.frostetsky.cloudstorage.dto.ErrorResponse;
import com.frostetsky.cloudstorage.excepiton.UserAlreadyExistException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


@Slf4j
@ControllerAdvice
public class ErrorHandlerController {


    @ExceptionHandler(UserAlreadyExistException.class)
    public ResponseEntity handleCreateUserException(UserAlreadyExistException e) {
        log.error("Ошибка при создании пользователя: {}", e.getMessage(), e);
        return ResponseEntity.status(e.getStatusCode()).body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity handleCreateUserException(BadCredentialsException e) {
        log.error("Ошибка авторизации: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("Неверный логин или пароль"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity handleValidationExceptions(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        log.error("Ошибка валидации: {}", e.getMessage(), e);
        return ResponseEntity.badRequest().body(new ErrorResponse(message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity handleOtherException(Exception e) {
        log.error("Получена непредвиденная ошибка {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("Произошла непредвиденная ошибка"));
    }
}
