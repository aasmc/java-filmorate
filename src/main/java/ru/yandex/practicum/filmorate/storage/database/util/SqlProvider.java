package ru.yandex.practicum.filmorate.storage.database.util;

import org.springframework.stereotype.Component;

@Component
public class SqlProvider {

    public String provideCheckUserIdSql() {
        return "SELECT id FROM Users WHERE id = ?";
    }

    public String provideFindUserByIdSql() {
        return "SELECT u.email u_email, " +
                "u.login u_login, " +
                "u.name u_name, " +
                "u.birthday u_bd, " +
                "uu.id f_id, " +
                "uu.email f_email, " +
                "uu.login f_login, " +
                "uu.name f_name, " +
                "uu.birthday f_bd " +
                "FROM Users u " +
                "LEFT JOIN FriendshipStatus fs ON u.id = fs.user_id " +
                "LEFT JOIN Users uu ON uu.id = fs.friend_id " +
                "WHERE u.id = ?";
    }

    public String provideGetCommonFriendsOfUsersSql() {
        return "WITH common_friend_ids as ( " +
                "SELECT f.friend_id f_id FROM Users u " +
                "INNER JOIN FriendshipStatus f ON u.id = f.user_id " +
                "WHERE u.id = ? " +
                "INTERSECT " +
                "SELECT ff.friend_id f_id FROM Users uu " +
                "INNER JOIN FriendshipStatus ff ON uu.id = ff.user_id " +
                "WHERE uu.id = ?) " +
                "SELECT u.id u_id, " +
                "u.email u_email, " +
                "u.login u_login, " +
                "u.name u_name, " +
                "u.birthday u_bd, " +
                "uu.id f_id, " +
                "uu.email f_email, " +
                "uu.login f_login, " +
                "uu.name f_name, " +
                "uu.birthday f_bd " +
                "FROM Users u " +
                "LEFT JOIN FriendshipStatus fs ON u.id = fs.user_id " +
                "LEFT JOIN Users uu ON uu.id = fs.friend_id " +
                "WHERE u.id IN (SELECT f_id FROM common_friend_ids)";
    }

    public String provideGetFriendsOfUserSql() {
        return "SELECT u.id u_id, " +
                "u.email u_email, " +
                "u.login u_login, " +
                "u.name u_name, " +
                "u.birthday u_bd, " +
                "uu.id f_id, " +
                "uu.email f_email, " +
                "uu.login f_login, " +
                "uu.name f_name, " +
                "uu.birthday f_bd " +
                "FROM Users u " +
                "LEFT JOIN FriendshipStatus fs on u.id = fs.user_id " +
                "LEFT JOIN Users uu on uu.id = fs.friend_id " +
                "WHERE u.id IN (SELECT ffs.friend_id FROM Users uuu " +
                "INNER JOIN FriendshipStatus ffs ON uuu.id = ffs.user_id " +
                "WHERE uuu.id = ?)";
    }

    public String provideFindAllUsersSql() {
        return "SELECT u.id u_id, " +
                "u.email u_email, " +
                "u.login u_login, " +
                "u.name u_name, " +
                "u.birthday u_bd, " +
                "uu.id f_id, " +
                "uu.email f_email, " +
                "uu.login f_login, " +
                "uu.name f_name, " +
                "uu.birthday f_bd " +
                "FROM Users u " +
                "LEFT JOIN FriendshipStatus fs ON u.id = fs.user_id " +
                "LEFT JOIN Users uu ON uu.id = fs.friend_id";
    }

    public String provideDeleteUserFriendsSql() {
        return "DELETE FROM FriendshipStatus WHERE user_id = ?";
    }

    public String provideDeleteSingleUserFriendSql() {
        return "DELETE FROM FriendshipStatus WHERE user_id = ? AND friend_id = ?";
    }

    public String provideUpdateUserSql() {
        return "UPDATE Users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
    }

    public String provideSaveUserFriendsSql() {
        return "INSERT INTO FriendshipStatus (user_id, friend_id) VALUES (?, ?)";
    }

    public String provideDeleteFilmGenresSql() {
        return "DELETE FROM FilmGenre WHERE film_id = ?";
    }

    public String provideSaveFilmGenresSql() {
        return "INSERT INTO FilmGenre (film_id, genre_id) VALUES (?, ?)";
    }

    public String provideUpdateFilmSql() {
        return "UPDATE Films SET name = ?, description = ?, release_date = ?, " +
                "duration = ?, rating_id = ? WHERE id = ?";
    }

    public String provideCheckFilmIdSql() {
        return "SELECT id FROM Films WHERE id = ?";
    }

    public String provideFindFilmByIdSql() {
        return "SELECT f.id film_id, " +
                "f.name film_name, " +
                "f.description film_descr, " +
                "f.release_date film_rel_d, " +
                "f.duration film_dur, " +
                "r.id mpa_id, " +
                "r.name mpa, " +
                "g.id genre_id, " +
                "g.name genre, " +
                "u.id u_id, " +
                "u.email u_email, " +
                "u.login u_login, " +
                "u.name u_name, " +
                "u.birthday u_bd, " +
                "uu.id f_id, " +
                "uu.email f_email, " +
                "uu.login f_login, " +
                "uu.name f_name, " +
                "uu.birthday f_bd, " +
                "FROM Films f " +
                "LEFT JOIN Ratings r ON f.rating_id = r.id " +
                "LEFT JOIN FilmGenre fg ON f.id = fg.film_id " +
                "LEFT JOIN Genres g ON g.id = fg.genre_id " +
                "LEFT JOIN FilmUserLikes ful ON ful.film_id = f.id " +
                "LEFT JOIN Users u ON u.id = ful.user_id " +
                "LEFT JOIN FriendshipStatus fs ON u.id = fs.user_id " +
                "LEFT JOIN Users uu ON uu.id = fs.friend_id " +
                "WHERE f.id = ?";
    }

    public String provideMostPopularFilmsSql() {
        return "WITH top_id_to_count AS ( " +
                "SELECT ff.id t_id, " +
                "COUNT(ful.film_id) cnt " +
                "FROM Films ff " +
                "LEFT JOIN FilmUserLikes ful ON ff.id = ful.film_id " +
                "GROUP BY t_id " +
                "ORDER BY cnt DESC " +
                "LIMIT ?" +
                ") " +
                "SELECT f.id film_id, " +
                "f.name film_name, " +
                "f.description film_descr, " +
                "f.release_date film_rel_d, " +
                "f.duration film_dur, " +
                "r.id mpa_id, " +
                "r.name mpa, " +
                "g.id genre_id, " +
                "g.name genre, " +
                "u.id u_id, " +
                "u.email u_email, " +
                "u.login u_login, " +
                "u.name u_name, " +
                "u.birthday u_bd, " +
                "uu.id f_id, " +
                "uu.email f_email, " +
                "uu.login f_login, " +
                "uu.name f_name, " +
                "uu.birthday f_bd, " +
                "FROM Films f " +
                "LEFT JOIN Ratings r ON f.rating_id = r.id " +
                "LEFT JOIN FilmGenre fg ON f.id = fg.film_id " +
                "LEFT JOIN Genres g ON g.id = fg.genre_id " +
                "LEFT JOIN FilmUserLikes ful ON ful.film_id = f.id " +
                "LEFT JOIN Users u ON u.id = ful.user_id " +
                "LEFT JOIN FriendshipStatus fs ON u.id = fs.user_id " +
                "LEFT JOIN Users uu ON uu.id = fs.friend_id " +
                "WHERE f.id IN (SELECT t_id FROM top_id_to_count)";
    }

    public String provideRemoveSingleUserLikeForFilmSql() {
        return "DELETE FROM FilmUserLikes WHERE film_id = ? AND user_id = ?";
    }

    public String provideRemoveAllUserLikesForFilmSql() {
        return "DELETE FROM FilmUserLikes WHERE film_id = ?";
    }

    public String provideAddUserLikesToFilmSql() {
        return "INSERT INTO FilmUserLikes (film_id, user_id) VALUES (?, ?)";
    }

    public String provideFilmFindAllSql() {
        return "SELECT f.id film_id, " +
                "f.name film_name, " +
                "f.description film_descr, " +
                "f.release_date film_rel_d, " +
                "f.duration film_dur, " +
                "r.id mpa_id, " +
                "r.name mpa, " +
                "g.id genre_id, " +
                "g.name genre, " +
                "u.id u_id, " +
                "u.email u_email, " +
                "u.login u_login, " +
                "u.name u_name, " +
                "u.birthday u_bd, " +
                "uu.id f_id, " +
                "uu.email f_email, " +
                "uu.login f_login, " +
                "uu.name f_name, " +
                "uu.birthday f_bd, " +
                "FROM Films f " +
                "LEFT JOIN Ratings r ON f.rating_id = r.id " +
                "LEFT JOIN FilmGenre fg ON f.id = fg.film_id " +
                "LEFT JOIN Genres g ON g.id = fg.genre_id " +
                "LEFT JOIN FilmUserLikes ful ON ful.film_id = f.id " +
                "LEFT JOIN Users u ON u.id = ful.user_id " +
                "LEFT JOIN FriendshipStatus fs ON u.id = fs.user_id " +
                "LEFT JOIN Users uu ON uu.id = fs.friend_id";
    }
}
