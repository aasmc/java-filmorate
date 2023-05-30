package ru.yandex.practicum.filmorate.testutil;

import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.model.RatingName;

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

    public static final String NEW_FILM_NAME = "New Film Name";
    public static final String NEW_FILM_DESCR = "New Film Description";
    public static LocalDate NEW_FILM_REL_D = LocalDate.of(1995, Month.SEPTEMBER, 1);
    public static final Long NEW_FILM_DUR = 1000L;
    public static final Rating NEW_FILM_RATING = Rating.builder().id(1L).name(RatingName.G).build();

    public static final String FILM1_NAME = "film1";
    public static final String FILM2_NAME = "film2";
    public static final String FILM3_NAME = "film3";
    public static final String FILM1_DESCR = "description1";
    public static final String FILM2_DESCR = "description2";
    public static final String FILM3_DESCR = "description3";
    public static LocalDate FILM1_REL_D = LocalDate.of(1990, Month.SEPTEMBER, 21);
    public static LocalDate FILM2_REL_D = LocalDate.of(1991, Month.SEPTEMBER, 21);
    public static LocalDate FILM3_REL_D = LocalDate.of(1992, Month.SEPTEMBER, 21);
    public static final Long FILM1_DUR = 100L;
    public static final Long FILM2_DUR = 101L;
    public static final Long FILM3_DUR = 102L;

    public static final Rating FILM1_RATING = Rating.builder().id(1L).name(RatingName.G).build();
    public static final Rating FILM2_RATING = Rating.builder().id(2L).name(RatingName.PG).build();
    public static final Rating FILM3_RATING = Rating.builder().id(3L).name(RatingName.PG_13).build();

}
