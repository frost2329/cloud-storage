package com.frostetsky.cloudstorage.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

import static com.frostetsky.cloudstorage.util.ValidationUtil.buildViolation;

public class PasswordValidator implements ConstraintValidator<Password, String> {
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^[a-zA-Z0-9!@#$%^&*(),.?\":{}|<>\\[\\\\\\]/`~+=\\-_';-]+$");

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password.isBlank())  {
            buildViolation(context, "Пароль не может быть пустым");
            return false;
        }
        if (password.length() < 5 || password.length() > 20) {
            buildViolation(context, "Пароль должно содержать от 5 до 20 символов");
            return false;
        }
        if (!PASSWORD_PATTERN.matcher(password).find()) {
            buildViolation(context, "Пароль содержит недопустимые символы");
            return false;
        }
        return true;
    }
}
