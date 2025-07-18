package com.frostetsky.cloudstorage.service;

import com.frostetsky.cloudstorage.dto.CreateUserRequest;
import com.frostetsky.cloudstorage.dto.CreateUserResponse;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {

    Long getUserIdByUsername(String username);

    CreateUserResponse createUser(CreateUserRequest dto);

    UserDetails loadUserByUsername(String username);
}
