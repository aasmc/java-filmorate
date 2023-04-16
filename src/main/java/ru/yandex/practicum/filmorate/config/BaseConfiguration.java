package ru.yandex.practicum.filmorate.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class BaseConfiguration {

    @Bean
    public Map<Long, Film> filmsMap() {
        return new ConcurrentHashMap<>();
    }

    @Bean
    public Map<Long, User> userMap() {
        return new ConcurrentHashMap<>();
    }

}
