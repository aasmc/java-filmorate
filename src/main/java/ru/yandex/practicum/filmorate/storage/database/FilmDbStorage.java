package ru.yandex.practicum.filmorate.storage.database;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import static ru.yandex.practicum.filmorate.storage.Constants.DB_FILM_STORAGE;

@Component
@Qualifier(DB_FILM_STORAGE)
@RequiredArgsConstructor
public class FilmDbStorage {

}
