package ru.yandex.practicum.filmorate.storage.database.dbutils;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FilmTable {
    private String id;
    private String name;
    private String descr;
    private String releaseDate;
    private String duration;
}
