package ru.yandex.practicum.filmorate.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = LoginValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginCorrect {
    String message() default "Invalid login.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
