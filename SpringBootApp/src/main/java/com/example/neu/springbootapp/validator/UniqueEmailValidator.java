package com.example.neu.springbootapp.validator;

import com.example.neu.springbootapp.Annotation.UniqueEmail;
import com.example.neu.springbootapp.model.Users;
import com.example.neu.springbootapp.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {


    @Autowired
    UsersRepository usersRepository;

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        Users account = usersRepository.findByUsername(s);
        if(account == null) return true;
        return false;
    }
}
