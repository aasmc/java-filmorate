package ru.yandex.practicum.filmorate.storage.database.util;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserColumns {
    private String id;
    private String email;
    private String login;
    private String name;
    private String birthday;
}
