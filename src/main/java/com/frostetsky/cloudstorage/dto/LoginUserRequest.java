package com.frostetsky.cloudstorage.dto;

import com.frostetsky.cloudstorage.validation.Password;
import com.frostetsky.cloudstorage.validation.Username;

public record LoginUserRequest(@Username String username,
                               @Password String password) {
}
