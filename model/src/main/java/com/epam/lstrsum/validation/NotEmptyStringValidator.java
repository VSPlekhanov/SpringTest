package com.epam.lstrsum.validation;

import com.epam.lstrsum.annotation.NotEmptyString;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static java.util.Objects.isNull;

public class NotEmptyStringValidator implements ConstraintValidator<NotEmptyString, String> {

    @Override
    public void initialize(NotEmptyString notEmptyString) {

    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        return !isNull(s) && !s.trim().isEmpty();
    }
}
