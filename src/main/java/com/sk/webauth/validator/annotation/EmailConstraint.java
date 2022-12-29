package com.sk.webauth.validator.annotation;

import com.sk.webauth.validator.EmailConstraintValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = EmailConstraintValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface EmailConstraint {
    int size() default 45;
    String message() default "Invalid Email address";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
