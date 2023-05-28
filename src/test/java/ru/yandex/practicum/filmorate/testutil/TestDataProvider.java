package ru.yandex.practicum.filmorate.testutil;

import ru.yandex.practicum.filmorate.model.User;

public class TestDataProvider {
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
