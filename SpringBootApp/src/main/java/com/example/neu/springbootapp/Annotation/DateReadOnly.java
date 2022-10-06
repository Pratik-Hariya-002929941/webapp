package com.example.neu.springbootapp.Annotation;

import com.example.neu.springbootapp.validator.DateReadOnlyValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(
        validatedBy = { DateReadOnlyValidator.class }
)
public @interface DateReadOnly {
    String message() default "account_created and account_updated is readonly property";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
