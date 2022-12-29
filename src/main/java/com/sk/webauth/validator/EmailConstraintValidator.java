package com.sk.webauth.validator;

import com.sk.webauth.validator.annotation.EmailConstraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmailConstraintValidator implements ConstraintValidator<EmailConstraint, String> {

    private static String emailDomain;
    private int size;

    @Bean
    public static String getEmailDomain(@Value("${web.auth.company.domain}") String ed) {
        emailDomain = ed;
        return emailDomain;
    }

    @Override
    public void initialize(EmailConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        size = constraintAnnotation.size();
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        constraintValidatorContext.disableDefaultConstraintViolation();

        if (s.length() < emailDomain.length() + 2) {
            customMessageForValidation(constraintValidatorContext, "Length of email is shorter");
            return false;
        }
        if (s.length() > size) {
            customMessageForValidation(constraintValidatorContext, "Length of email is longer than allowed");
            return false;
        }
        if (!s.matches("^[A-Za-z0-9._%+-]+@" + emailDomain + "$")) {
            customMessageForValidation(constraintValidatorContext, "Incorrect email type");
            return false;
        }
        return true;
    }

    private void customMessageForValidation(ConstraintValidatorContext constraintContext, String message) {
        // build new violation message and add it
        constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }
}
