package com.hissah.Controllers.support;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class RequestValidationSupport {

    private final Validator validator;

    public <T> void validate(T request) {
        Set<ConstraintViolation<T>> violations =
                validator.validate(request);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(
                    violations
            );
        }
    }
}
