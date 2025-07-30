package com.frostetsky.cloudstorage.integration.service;

import com.frostetsky.cloudstorage.dto.CreateUserRequest;
import com.frostetsky.cloudstorage.dto.CreateUserResponse;
import com.frostetsky.cloudstorage.excepiton.UserAlreadyExistException;
import com.frostetsky.cloudstorage.model.User;
import com.frostetsky.cloudstorage.repository.UserRepository;
import com.frostetsky.cloudstorage.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
public class UserServiceTest {

    @Autowired
    private UserService userService;
    @Autowired
    private DataSource dataSource;
    @Autowired
    private UserRepository userRepository;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");


    @BeforeEach
    void cleanTableUsers() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             Statement statement = conn.createStatement()) {
            statement.execute("truncate table users cascade");
            statement.execute("alter sequence users_id_seq restart with 1");
        }
    }

    @Test
    void createUser_Test() {
        CreateUserRequest userDto = new CreateUserRequest("test_user", "password");
        CreateUserResponse user = userService.createUser(userDto);
        assertEquals(userDto.username(), user.username());
        List<User> users = userRepository.findAll();
        assertTrue(users.stream().anyMatch(u -> userDto.username().equals(u.getUsername())));
    }

    @Test
    void createUser_TestWithExistedUsername() {
        CreateUserRequest userDto = new CreateUserRequest("test_user", "password");
        assertThrows(UserAlreadyExistException.class, () -> {
            userService.createUser(userDto);
            userService.createUser(userDto);
        });
    }

    @Test
    void createUser_TestWithAnotherError() {
        CreateUserRequest userDto = new CreateUserRequest(
                "test_user_test_user_test_user_test_user_test_user_test_user_test_user_test_user_" +
                "test_user_test_user_test_user_test_user_test_user_test_user_test_user_test_user_test_user_",
                "password");
        assertThrows(UserAlreadyExistException.class, () -> userService.createUser(userDto));
    }

    @Test
    void getUserById_Test() {
        CreateUserRequest userDto = new CreateUserRequest("test_user", "password");
        CreateUserResponse user = userService.createUser(userDto);
        assertEquals(1, userService.getUserIdByUsername(user.username()));
    }

    @Test
    void loadUserByUsername_Test() {
        CreateUserRequest userDto = new CreateUserRequest("test_user", "password");
        CreateUserResponse user = userService.createUser(userDto);
        UserDetails userDetails = userService.loadUserByUsername(user.username());
        assertEquals(userDetails.getUsername(), userDto.username());
    }
}
