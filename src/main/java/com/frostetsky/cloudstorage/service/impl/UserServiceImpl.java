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
            log.info("Получение ID пользователя: username={}", username);
            return userRepository.getUserByUsername(username)
                    .orElseThrow(() -> {
                        log.error("Пользователь не найден: username={}", username);
                        return new UsernameNotFoundException("Пользователь с username %s не найден".formatted(username));
                    })
                    .getId();
        } catch (Exception e) {
            log.error("Ошибка при получении пользователя: username={}", username);
            throw new UserServiceException("Ошибка при загрузки пользователя", e);
        }
    }

    @Transactional
    public CreateUserResponse createUser(CreateUserRequest dto) {
        log.info("Создание нового пользователя: username={}", dto.username());
        try {
            User user = userRepository.save(User.builder()
                    .username(dto.username())
                    .password(passwordEncoder.encode(dto.password()))
                    .build());
            log.info("Пользователь успешно создан: username={}", dto.username());
            return new CreateUserResponse(user.getUsername());
        } catch (DataIntegrityViolationException e) {
            log.error("Ошибка при создании пользователя, имя пользователя занято: username={}", dto.username());
            throw new UserAlreadyExistException(e);
        } catch (Exception e) {
            log.error("Ошибка при создании пользователя: username={}", dto.username());
            throw new UserServiceException("Ошибка при создании пользователя", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            log.info("Загрузка пользователя: username={}", username);
            User user = userRepository.getUserByUsername(username)
                    .orElseThrow(() -> {
                        log.error("Пользователь  не найден: username={}", username);
                        return new UsernameNotFoundException("Пользователь не найден");
                    });
            return new CustomUserDetails(user);
        } catch (UsernameNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Ошибка при загрузки пользователя: username={}", username);
            throw new UserServiceException("Ошибка при загрузки пользователя", e);
        }

    }
}
