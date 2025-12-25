package com.frostetsky.cloudstorage.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

import static com.frostetsky.cloudstorage.util.ValidationUtil.buildViolation;

public class QueryValidator implements ConstraintValidator<Query, String> {
    private static final Pattern INVALID_CHARS = Pattern.compile("[<>:\"\\\\|?*/]");

    @Override
    public boolean isValid(String query, ConstraintValidatorContext context) {
        if (query.isBlank()) {
            buildViolation(context, "Поисковый запрос не передан");
            return false;
        }
        if (INVALID_CHARS.matcher(query).find()) {
            buildViolation(context, "Поисковый запрос содержит недопустимые символы");
            return false;
        }
        return true;
    }
}
