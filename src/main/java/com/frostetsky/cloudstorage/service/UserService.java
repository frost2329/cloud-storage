package com.frostetsky.cloudstorage.service;

import com.frostetsky.cloudstorage.dto.CreateUserDto;
import com.frostetsky.cloudstorage.dto.LoginUserDto;
import com.frostetsky.cloudstorage.entity.User;
import com.frostetsky.cloudstorage.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;

    @Transactional
    public void createUser(CreateUserDto dto) {
        User user = userRepository.save(User.builder()
                .username(dto.username())
                .password(dto.password())
                .build());
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.getUserByUsername(username).map(user -> new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.emptyList()
        )).orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
    }
}
