package ru.yandex.practicum.filmorate.storage.database.util;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FilmColumns {
    private String id;
    private String name;
    private String descr;
    private String releaseDate;
    private String duration;
}
