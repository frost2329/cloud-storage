package com.frostetsky.cloudstorage.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = FileValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface File {

    String message() default "Ошибка. Невалидное тело запроса";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
