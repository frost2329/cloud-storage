package com.frostetsky.cloudstorage.service.impl;

import com.frostetsky.cloudstorage.dto.CreateUserRequest;
import com.frostetsky.cloudstorage.dto.CreateUserResponse;
import com.frostetsky.cloudstorage.model.CustomUserDetails;
import com.frostetsky.cloudstorage.model.User;
import com.frostetsky.cloudstorage.excepiton.UserAlreadyExistException;
import com.frostetsky.cloudstorage.repository.UserRepository;
import com.frostetsky.cloudstorage.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Long getUserIdByUsername(String username) {
        return userRepository.getUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь с username %s не найден".formatted(username)))
                .getId();
    }

    @Transactional
    public CreateUserResponse createUser(CreateUserRequest dto) {
        try {
            User user = userRepository.save(User.builder()
                    .username(dto.username())
                    .password(passwordEncoder.encode(dto.password()))
                    .build());
            return new CreateUserResponse(user.getUsername());
        } catch (DataIntegrityViolationException e) {
            throw new UserAlreadyExistException(e);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.getUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
        return new CustomUserDetails(user);
    }
}
