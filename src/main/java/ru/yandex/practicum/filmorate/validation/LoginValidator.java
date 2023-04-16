package ru.yandex.practicum.filmorate.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class LoginValidator implements ConstraintValidator<LoginCorrect, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return !value.isEmpty() && !value.contains(" ");
    }

}
