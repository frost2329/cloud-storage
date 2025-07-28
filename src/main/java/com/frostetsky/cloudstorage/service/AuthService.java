package com.frostetsky.cloudstorage.service;

import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
    void authenticateAndCreateSession(String username, String password, HttpServletRequest request);
}
