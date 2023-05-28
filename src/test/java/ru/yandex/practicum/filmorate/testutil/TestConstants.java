package ru.yandex.practicum.filmorate.testutil;

import java.time.LocalDate;
import java.time.Month;

public class TestConstants {
    public static final String LONG_DESCRIPTION = "Lorem Ipsum is simply dummy text of the " +
            "printing and typesetting industry. Lorem Ipsum has been the industry's standard " +
            "dummy text ever since the 1500s, when an unknown printer took a galley of type " +
            "and scrambled it to make a type specimen book. It has survived not only five " +
            "centuries, but also the leap into electronic typesetting, remaining essentially " +
            "unchanged. It was popularised in the 1960s with the release of Letraset sheets " +
            "containing Lorem Ipsum passages, and more recently with desktop publishing software" +
            " like Aldus PageMaker including versions of Lorem Ipsum.";

    public static final LocalDate THRESHOLD_DATE = LocalDate.of(1895, Month.DECEMBER, 27);

    public static final String NEW_USER_NAME = "New User Name";
    public static final String NEW_USER_EMAIL = "new@email.com";
    public static final String NEW_USER_LOGIN = "new";
    public static LocalDate NEW_USER_BIRTHDAY = LocalDate.of(1998, Month.DECEMBER, 31);
    public static final String USER1_EMAIL = "user1@email.com";
    public static final String USER2_EMAIL = "user2@email.com";
    public static final String USER3_EMAIL = "user3@email.com";
    public static final String USER1_LOGIN = "user1";
    public static final String USER2_LOGIN = "user2";
    public static final String USER3_LOGIN = "user3";
    public static final String USER1_NAME = "Tom";
    public static final String USER2_NAME = "Max";
    public static final String USER3_NAME = "Alex";
    public static LocalDate USER1_BD = LocalDate.of(1990, Month.SEPTEMBER, 21);
    public static LocalDate USER2_BD = LocalDate.of(1991, Month.SEPTEMBER, 21);
    public static LocalDate USER3_BD = LocalDate.of(1992, Month.SEPTEMBER, 21);
}
