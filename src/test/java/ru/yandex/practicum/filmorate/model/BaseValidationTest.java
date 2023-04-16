package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.BeforeAll;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

public class BaseValidationTest {
    public static Validator validator;

    @BeforeAll
    static void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
}
