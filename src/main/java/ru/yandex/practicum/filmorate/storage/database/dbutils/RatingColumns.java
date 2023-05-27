package ru.yandex.practicum.filmorate.storage.database.dbutils;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RatingColumns {
    private String id;
    private String name;
}
