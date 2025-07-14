package com.frostetsky.cloudstorage.service;

import com.frostetsky.cloudstorage.dto.CreateUserRequest;
import com.frostetsky.cloudstorage.dto.CreateUserResponse;
import com.frostetsky.cloudstorage.entity.User;
import com.frostetsky.cloudstorage.excepiton.UserAlreadyExistException;
import com.frostetsky.cloudstorage.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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
        return userRepository.getUserByUsername(username)
                .map(user -> new org.springframework.security.core.userdetails.User(
                        user.getUsername(),
                        user.getPassword(),
                        Collections.emptyList()
                )).orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
    }
}
