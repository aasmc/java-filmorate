package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import ru.yandex.practicum.filmorate.validation.ReleaseDateCorrect;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Заметка для себя на будущее: пока не используем Spring Data JPA + Hibernate, можно оставить аннотацию @Data,
 * потом придется сделать рефакторинг. Подробнее тут:
 * https://habr.com/ru/companies/haulmont/articles/564682/
 * https://vladmihalcea.com/the-best-way-to-implement-equals-hashcode-and-tostring-with-jpa-and-hibernate/
 */
@Data
@Builder
@Jacksonized
public class Film {
    /**
     * Специально сделал Long, а не long, так как, наверняка, в дальнейшем будем
     * сохранять это в базу данных с помощью Spring Data и Hibernate.
     */
    private Long id;
    @NotBlank(message = "Название не может быть пустым.")
    private String name;
    @Size(max = 200, message = "Максимальная длина описания — 200 символов.")
    private String description;

    @ReleaseDateCorrect(message = "Дата релиза — не раньше 28 декабря 1895 года.")
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть положительной.")
    private long duration;

    @Builder.Default
    private Set<Long> userLikes = new HashSet<>();

    public void addLikeByUserWithId(Long userId) {
        userLikes.add(userId);
    }

    public void removeLikeByUserWithId(Long userId) {
        userLikes.remove(userId);
    }

    public int getLikesCount() {
        return userLikes.size();
    }

}
