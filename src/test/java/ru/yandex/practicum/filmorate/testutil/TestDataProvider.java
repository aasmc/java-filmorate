package ru.yandex.practicum.filmorate.testutil;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

public class TestDataProvider {

    public static Film newFilm() {
        return Film.builder()
                .name(TestConstants.NEW_FILM_NAME)
                .description(TestConstants.NEW_FILM_DESCR)
                .releaseDate(TestConstants.NEW_FILM_REL_D)
                .duration(TestConstants.NEW_FILM_DUR)
                .mpa(TestConstants.NEW_FILM_RATING)
                .build();
    }

    public static Film filmOne() {
        return Film.builder()
                .name(TestConstants.FILM1_NAME)
                .description(TestConstants.FILM1_DESCR)
                .releaseDate(TestConstants.FILM1_REL_D)
                .duration(TestConstants.FILM1_DUR)
                .mpa(TestConstants.FILM1_RATING)
                .build();
    }

    public static Film filmTwo() {
        return Film.builder()
                .name(TestConstants.FILM2_NAME)
                .description(TestConstants.FILM2_DESCR)
                .releaseDate(TestConstants.FILM2_REL_D)
                .duration(TestConstants.FILM2_DUR)
                .mpa(TestConstants.FILM2_RATING)
                .build();
    }

    public static Film filmThree() {
        return Film.builder()
                .name(TestConstants.FILM3_NAME)
                .description(TestConstants.FILM3_DESCR)
                .releaseDate(TestConstants.FILM3_REL_D)
                .duration(TestConstants.FILM3_DUR)
                .mpa(TestConstants.FILM3_RATING)
                .build();
    }

    public static User newUser() {
        return User.builder()
                .email(TestConstants.NEW_USER_EMAIL)
                .name(TestConstants.NEW_USER_NAME)
                .login(TestConstants.NEW_USER_LOGIN)
                .birthday(TestConstants.NEW_USER_BIRTHDAY)
                .build();
    }

    public static User userOne() {
        return User.builder()
                .email(TestConstants.USER1_EMAIL)
                .name(TestConstants.USER1_NAME)
                .login(TestConstants.USER1_LOGIN)
                .birthday(TestConstants.USER1_BD)
                .build();
    }

    public static User userTwo() {
        return User.builder()
                .email(TestConstants.USER2_EMAIL)
                .name(TestConstants.USER2_NAME)
                .login(TestConstants.USER2_LOGIN)
                .birthday(TestConstants.USER2_BD)
                .build();
    }

    public static User userThree() {
        return User.builder()
                .email(TestConstants.USER3_EMAIL)
                .name(TestConstants.USER3_NAME)
                .login(TestConstants.USER3_LOGIN)
                .birthday(TestConstants.USER3_BD)
                .build();
    }
}
