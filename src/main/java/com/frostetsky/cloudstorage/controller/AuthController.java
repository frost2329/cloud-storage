package com.frostetsky.cloudstorage.controller;

import com.frostetsky.cloudstorage.dto.CreateUserDto;
import com.frostetsky.cloudstorage.dto.CreateUserResponse;
import com.frostetsky.cloudstorage.dto.ErrorResponse;
import com.frostetsky.cloudstorage.dto.LoginUserDto;
import com.frostetsky.cloudstorage.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/sign-up")
    public ResponseEntity registration(@RequestBody CreateUserDto dto,
                                       HttpServletRequest request) {
        try {
            CreateUserResponse createUserResponse = userService.createUser(dto);
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.username(), dto.password()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            request.getSession(true);
            return ResponseEntity.status(HttpStatus.CREATED).body(createUserResponse);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/sign-in")
    public ResponseEntity login(@RequestBody LoginUserDto dto,
                                HttpServletRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.username(), dto.password()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            request.getSession(true);
            return ResponseEntity.status(HttpStatus.OK).body(new CreateUserResponse(dto.username()));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(e.getMessage()));
        }
    }
}
