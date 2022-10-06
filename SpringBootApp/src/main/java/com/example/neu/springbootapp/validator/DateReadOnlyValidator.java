package com.example.neu.springbootapp.validator;

import com.example.neu.springbootapp.Annotation.DateReadOnly;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Date;

public class DateReadOnlyValidator implements ConstraintValidator<DateReadOnly, Date> {
    @Override
    public boolean isValid(Date d, ConstraintValidatorContext constraintValidatorContext) {
        if(d == null) return true;
        return false;
    }
}
