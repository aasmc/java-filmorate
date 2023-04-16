package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import javax.validation.ConstraintViolation;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class UserValidationTests extends BaseValidationTest {

    @Test
    void whenValidUser_ConstraintViolationsEmpty() {
        User user = User
                .builder()
                .email("email@email.com")
                .login("login")
                .name("name")
                .birthday(LocalDate.now().minusDays(10))
                .build();
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("invalidUsers")
    void whenInvalidUser_ConstraintViolationsNotEmpty(User user) {
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertThat(violations).isNotEmpty();
    }

    private static Stream<User> invalidUsers() {
        return Stream.of(
                User // invalid email
                        .builder()
                        .email("email.email")
                        .login("login")
                        .name("name")
                        .birthday(LocalDate.now().minusDays(10))
                        .build(),
                User // invalid login
                        .builder()
                        .email("email@email.com")
                        .login("invalid login")
                        .name("name")
                        .birthday(LocalDate.now().minusDays(10))
                        .build(),
                User // invalid birthDay
                        .builder()
                        .email("email@emai.com")
                        .login("login")
                        .name("name")
                        .birthday(LocalDate.now().plusDays(1))
                        .build(),
                User
                        .builder()
                        .email("incorrect email")
                        .login("incorrect login")
                        .name("name")
                        .birthday(LocalDate.now().plusDays(10))
                        .build()
        );
    }
}
