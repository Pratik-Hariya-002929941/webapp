package com.example.neu.springbootapp.validator;

import com.example.neu.springbootapp.Annotation.ReadOnly;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.UUID;

public class ReadOnlyValidator implements ConstraintValidator<ReadOnly, UUID> {

    @Override
    public boolean isValid(UUID uuid, ConstraintValidatorContext constraintValidatorContext) {
        if(uuid == null) return true;
        return false;
    }
}
