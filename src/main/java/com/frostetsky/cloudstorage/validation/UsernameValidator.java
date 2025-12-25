package com.frostetsky.cloudstorage.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

import static com.frostetsky.cloudstorage.util.ValidationUtil.buildViolation;

public class UsernameValidator implements ConstraintValidator<Username, String> {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9]+[a-zA-Z_0-9]*[a-zA-Z0-9]+$");

    @Override
    public boolean isValid(String username, ConstraintValidatorContext context) {
        if (username.isBlank()) {
            buildViolation(context, "Имя пользователя не может быть пустым");
            return false;
        }
        if (username.length() < 5 || username.length() > 20) {
            buildViolation(context, "Имя пользователя должно содержать от 5 до 20 символов");
            return false;
        }
        if (!USERNAME_PATTERN.matcher(username).find()) {
            buildViolation(context, "Имя пользователя содержит недопустимые символы");
            return false;
        }
        return true;
    }
}
