package com.epam.lstrsum.annotation;

import com.epam.lstrsum.validation.NotEmptyStringValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = NotEmptyStringValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface NotEmptyString {
    String message() default "Empty string parameter value";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
