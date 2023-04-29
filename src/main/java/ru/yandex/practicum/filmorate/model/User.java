package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.validation.LoginCorrect;

import javax.validation.constraints.Email;
import javax.validation.constraints.Past;
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
public class User {
    /**
     * Специально сделал Long, а не long, так как, наверняка, в дальнейшем будем
     * сохранять это в базу данных с помощью Spring Data и Hibernate.
     */
    private Long id;
    @Email(message = "электронная почта не может быть пустой и должна содержать символ @.")
    private String email;
    @LoginCorrect(message = "Логин не может быть пустым и содержать пробелы.")
    private String login;
    private String name;
    @Past
    private LocalDate birthday;
    private final Set<Long> friends = new HashSet<>();

    public void addFriend(User user) {
        friends.add(user.getId());
        user.getFriends().add(this.id);
    }

    public void removeFriend(User user) {
        friends.remove(user.getId());
    }

    public Set<Long> commonFriendsWith(User user) {
        Set<Long> intersection = new HashSet<>(friends);
        intersection.retainAll(user.getFriends());
        return intersection;
    }
}
