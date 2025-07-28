package com.frostetsky.cloudstorage.controller;

import com.frostetsky.cloudstorage.dto.*;
import com.frostetsky.cloudstorage.service.AuthService;
import com.frostetsky.cloudstorage.service.DirectoryService;
import com.frostetsky.cloudstorage.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthService authService;
    private final DirectoryService directoryService;

    @PostMapping("/sign-up")
    public ResponseEntity<CreateUserResponse> registration(@RequestBody @Validated CreateUserRequest dto,
                                                           HttpServletRequest request) {
        CreateUserResponse createUserResponse = userService.createUser(dto);
        directoryService.createBaseDirectory(dto.username());
        authService.authenticateAndCreateSession(dto.username(), dto.password(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createUserResponse);
    }

    @PostMapping("/sign-in")
    public ResponseEntity<LoginUserResponse> login(@RequestBody @Validated LoginUserRequest dto,
                                                   HttpServletRequest request) {
        HttpSession existingSession = request.getSession(false);
        if (existingSession != null) {
            existingSession.invalidate();
        }
        authService.authenticateAndCreateSession(dto.username(), dto.password(), request);
        return ResponseEntity.status(HttpStatus.OK).body(new LoginUserResponse(dto.username()));

    }
}
