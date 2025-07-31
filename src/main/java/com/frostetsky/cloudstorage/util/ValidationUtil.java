package com.frostetsky.cloudstorage.util;

import jakarta.validation.ConstraintValidatorContext;

public class ValidationUtil {
    public static void buildViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addConstraintViolation();
    }
}
