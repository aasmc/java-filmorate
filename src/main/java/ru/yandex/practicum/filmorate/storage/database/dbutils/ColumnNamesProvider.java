package ru.yandex.practicum.filmorate.storage.database.dbutils;

import org.springframework.stereotype.Component;

@Component
public class ColumnNamesProvider {
    public UserTable provideUserColumns() {
        return UserTable.builder()
                .id("u_id")
                .email("u_email")
                .login("u_login")
                .name("u_name")
                .birthday("u_bd")
                .build();
    }

    public UserTable provideFriendColumns() {
        return UserTable.builder()
                .id("f_id")
                .email("f_email")
                .login("f_login")
                .name("f_name")
                .birthday("f_bd")
                .build();
    }

    public RatingTable provideRatingColumns() {
        return RatingTable.builder()
                .id("mpa_id")
                .name("mpa")
                .build();
    }

    public GenreTable provideGenreColumns() {
        return GenreTable.builder()
                .id("genre_id")
                .name("genre")
                .build();
    }

    public FilmTable provideFilmColumns() {
        return FilmTable.builder()
                .id("film_id")
                .name("film_name")
                .descr("film_descr")
                .releaseDate("film_rel_d")
                .duration("film_dur")
                .build();
    }
}
