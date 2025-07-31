package com.frostetsky.cloudstorage.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

import static com.frostetsky.cloudstorage.util.ValidationUtil.buildViolation;

public class PathValidator implements ConstraintValidator<Path, String> {

    private static final Pattern INVALID_CHARS = Pattern.compile("[<>:\"\\\\|?*/]");

    @Override
    public boolean isValid(String path, ConstraintValidatorContext context) {
        if (path == null)  {
            buildViolation(context, "Путь не передан");
            return false;
        }
        if (path.isEmpty()) {
            return true;
        }
        String[] parts = path.split("/");

        for (String part : parts) {
            if (part.isEmpty()) {
                buildViolation(context, "Переданный недопустимый путь");
                return false;
            }
            if (INVALID_CHARS.matcher(part).find()) {
                buildViolation(context, "Переданный недопустимый путь");
                return false;
            }
        }
        return true;
    }
}



