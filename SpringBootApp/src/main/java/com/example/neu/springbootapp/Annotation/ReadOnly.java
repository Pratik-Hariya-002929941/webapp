package com.example.neu.springbootapp.Annotation;

import com.example.neu.springbootapp.validator.ReadOnlyValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Pratik Hariya
 */

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(
        validatedBy = { ReadOnlyValidator.class }
)
public @interface ReadOnly {
    String message() default "You are not allowed to pass ID. Id will be generated automatically.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
