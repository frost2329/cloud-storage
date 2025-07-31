package com.frostetsky.cloudstorage.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = PathValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Path {

    String message() default "Ошибка. Передан недопустимый путь";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
