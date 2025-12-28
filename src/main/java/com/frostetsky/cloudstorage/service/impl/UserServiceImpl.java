package com.frostetsky.cloudstorage.service.impl;

import com.frostetsky.cloudstorage.dto.CreateUserRequest;
import com.frostetsky.cloudstorage.dto.CreateUserResponse;
import com.frostetsky.cloudstorage.excepiton.UserServiceException;
import com.frostetsky.cloudstorage.model.CustomUserDetails;
import com.frostetsky.cloudstorage.model.User;
import com.frostetsky.cloudstorage.excepiton.UserAlreadyExistException;
import com.frostetsky.cloudstorage.repository.UserRepository;
import com.frostetsky.cloudstorage.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Long getUserIdByUsername(String username) {
        try {
            log.debug("Fetching user id by username: username={}", username);
            return userRepository.getUserByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException(
                            "User with username %s was not found".formatted(username)
                    ))
                    .getId();
        } catch (UsernameNotFoundException e) {
            log.debug("User not found while fetching id: username={}", username);
            throw e;
        } catch (Exception e) {
            log.error("Failed to fetch user id by username: username={}", username, e);
            throw new UserServiceException("Failed to load user", e);
        }
    }

    @Transactional
    public CreateUserResponse createUser(CreateUserRequest dto) {
        log.info("Creating new user: username={}", dto.username());
        try {
            User user = userRepository.save(User.builder()
                    .username(dto.username())
                    .password(passwordEncoder.encode(dto.password()))
                    .build());
            log.info("User created successfully: username={}", dto.username());
            return new CreateUserResponse(user.getUsername());
        } catch (DataIntegrityViolationException e) {
            log.warn("User creation failed: username already exists: username={}", dto.username());
            throw new UserAlreadyExistException("User already exist", e);
        } catch (Exception e) {
            log.error("Failed to create user: username={}", dto.username(), e);
            throw new UserServiceException("Failed to create user", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            log.debug("Loading user details by username: username={}", username);
            User user = userRepository.getUserByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User was not found"));
            return new CustomUserDetails(user);
        } catch (UsernameNotFoundException e) {
            log.debug("User not found while loading user details: username={}", username);
            throw e;
        } catch (Exception e) {
            log.error("Failed to load user details by username: username={}", username, e);
            throw new UserServiceException("Failed to load user", e);
        }

    }
}
