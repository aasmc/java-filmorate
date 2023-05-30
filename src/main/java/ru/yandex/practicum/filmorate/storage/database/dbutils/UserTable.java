package ru.yandex.practicum.filmorate.storage.database.dbutils;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserTable {
    private String id;
    private String email;
    private String login;
    private String name;
    private String birthday;
}
