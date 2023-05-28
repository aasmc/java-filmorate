# java-filmorate

### DB Diagram of the Filmorate Project:

![DB Diagram of the Filmorate project](https://github.com/aasmc/java-filmorate/blob/genres-friends/art/diagram.png)

### Films
Таблица Films связана с таблицей Genres связью многие-ко-многим, то есть у одного фильма
может быть несколько жанров, один жанр может быть у нескольких фильмов. Это сделано через
связующую таблицу FilmGenre, которая содержит в качестве PK - film_id и genre_id.

Также таблица Films связна отношением один-к-одному с таблицей Ratings, то есть у фильма может
быть только один рейтинг. Это решение обсуждаемое: можно обойтись и дополнительным столбцом
в таблице Films, но тогда, если, скажем, придется добавить дату проставления рейтинга, придется 
добавлять и это поле к таблице Films, хотя по факту оно связано с таблицей Ratings.

### Users
Таблица Users связана с таблицей Films отношением многие-ко-многим, то есть один пользователь
может поставить лайки многим фильмам и у одного фильма могут быть лайки от многих пользователей.
Это сделано через связующую таблицу FilmUserLikes. 

"Дружба" между пользователями организована через отдельную таблицу FriendshipStatus,
(спасибо этому посту: https://dba.stackexchange.com/questions/135941/designing-a-friendships-database-structure-should-i-use-a-multivalued-column)
первичный ключ в которой состоит из двух полей: user_id (пользователь, который добавляет друга),
friend_id (пользователь, которого добавляют в друзья). friend_id
и user_id не могут быть одинаковыми, для этого при создании таблиц добавлен CHECK constraint.
Дружба считается подтвержденной, если в таблице FriendshipStatus есть две записи о дружбе - пользователя и друга,
с идентификаторами друг-друга в качестве friend_id. Если запись одна - значит дружба односторонняя и
требует подтверждения со стороны того, кого пригласили в друзья. 

### Пример:

1. Пользователь с ID 1 добавляет в друзья пользователя с ID 2 -> в БД появляется запись:
```text
user_id   friend_id   
   1         2        
```
2. Пользователь с ID 2 принимает приглашение -> в БД появляется еще одна запись:
```text
user_id   friend_id   
   2         1         
```

3. При удалении пользователя из системы, необходимо в рамках одной транзакции сначала удалить 
записи о друзьях, а после этого удалять запись о пользователе из таблицы БД.

### Прмеры запросов
1. Получить все фильмы:
```sql
SELECT f.id film_id,
       f.name film_name,
       f.description film_descr,
       f.release_date film_rel_d,
       f.duration film_dur,
       r.id mpa_id,
       r.name mpa,
       g.id genre_id,
       g.name genre,
       u.id u_id,
FROM Films f LEFT JOIN Ratings r ON f.rating_id = r.id
             LEFT JOIN FilmGenre fg ON f.id = fg.film_id
             LEFT JOIN Genres g ON g.id = fg.genre_id
             LEFT JOIN FilmUserLikes ful ON ful.film_id = f.id
             LEFT JOIN Users u ON u.id = ful.user_id;
```

2. Добавить лайк фильму с ID = 1, от пользователя с ID = 2
```sql
INSERT INTO FilmUserLikes (film_id, user_id) VALUES (1, 2);
```

3. Удалить у фильма с ID = 1 лайк пользователя с ID = 2
```sql
DELETE FROM FilmUserLikes WHERE film_id = 1 AND user_id = 2;
```
4. Получить 10 самых популярных фильмов:
```sql
WITH top_id_to_count AS (SELECT ff.id t_id, COUNT(ful.film_id) cnt
                         FROM Films ff
                         LEFT JOIN FilmUserLikes ful ON ff.id = ful.film_id
                         GROUP BY t_id
                         ORDER BY cnt DESC
                         LIMIT 10)
SELECT f.id film_id,
       f.name film_name,
       f.description film_descr,
       f.release_date film_rel_d,
       f.duration film_dur,
       r.id mpa_id,
       r.name mpa,
       g.id genre_id,
       g.name genre,
       u.id u_id,
FROM Films f LEFT JOIN Ratings r ON f.rating_id = r.id
             LEFT JOIN FilmGenre fg ON f.id = fg.film_id
             LEFT JOIN Genres g ON g.id = fg.genre_id
             LEFT JOIN FilmUserLikes ful ON ful.film_id = f.id
             LEFT JOIN Users u ON u.id = ful.user_id
WHERE f.id IN (SELECT t_id FROM top_id_to_count);
```
То же самое, но без жанров, рейтинга и лайков и в отсортированном по просмотрам порядке: 
```sql
WITH top_id_to_count AS (SELECT ff.id t_id, COUNT(ful.film_id) cnt
                         FROM "Films" ff
                                  LEFT JOIN "FilmUserLikes" ful ON ff.id = ful.film_id
                         GROUP BY t_id
                         ORDER BY cnt DESC
                         LIMIT 10)
SELECT f.id,
       f.name,
       f.description,
       f.release_date,
       f.duration
FROM Films f
    INNER JOIN top_id_to_count titc on f.id = titc.t_id
    ORDER BY titc.cnt DESC;
```
5. Получить всех пользователей
```sql
SELECT u.id u_id,
       u.email u_email,
       u.login u_login,
       u.name u_name,
       u.birthday u_bd,
       uu.id f_id,
       uu.email f_email,
       uu.login f_login,
       uu.name f_name,
       uu.birthday f_bd
    FROM Users u 
    LEFT JOIN FriendshipStatus fs on u.id = fs.user_id 
    LEFT JOIN Users uu on.uu.id = fs.friend_id;
```
6. Получить друзей пользователя с ID = 1
```sql
SELECT u.id u_id,
       u.email u_email,
       u.login u_login,
       u.name u_name,
       u.birthday u_bd,
       uu.id f_id,
       uu.email f_email,
       uu.login f_login,
       uu.name f_name,
       uu.birthday f_bd
FROM Users u
         LEFT JOIN FriendshipStatus fs on u.id = fs.user_id
         LEFT JOIN Users uu on.uu.id = fs.friend_id
WHERE u.id IN
      (SELECT ffs.friend_id
       FROM users uuu
       INNER JOIN FriendshipStatus ffs ON uuu.id = ffs.user_id
       WHERE uuu.id = 1);
```
7. Получить общих друзей пользователя с ID = 1 и пользователя с ID = 2
```sql
WITH common_friend_ids AS (
    SELECT f.friend_id f_id
    FROM users u
    INNER JOIN FriendshipStatus f ON u.id = f.user_id
    WHERE u.id = 1
    INTERSECT
    SELECT ff.friend_id f_id
    FROM users uu
    INNER JOIN FriendshipStatus ff ON uu.id = ff.user_id
    WHERE uu.id = 2
)
SELECT u.id u_id,
       u.email u_email,
       u.login u_login,
       u.name u_name,
       u.birthday u_bd,
       uu.id f_id,
       uu.email f_email,
       uu.login f_login,
       uu.name f_name,
       uu.birthday f_bd
FROM Users u
         LEFT JOIN FriendshipStatus fs on u.id = fs.user_id
         LEFT JOIN Users uu on.uu.id = fs.friend_id
WHERE u.id IN
(SELECT f_id FROM common_friend_ids); 
```
8. Пользователь с ID = 1 предлагает пользователю с ID = 2 дружбу:
```sql
INSERT INTO FriendshipStatus (user_id, friend_id, status)
VALUES (1, 2);
```
9. Пользователь с ID = 2 одобряет заявку пользователя с ID = 1:
```sql
INSERT INTO FriendshipStatus (user_id, friend_id)
VALUES (2, 1);
```
10. Пользователь с ID = 1 удаляет из друзей пользователя с ID = 2:
```sql
DELETE FROM FriendshipStatus 
WHERE user_id = 1 AND friend_id = 2;
```
11. Удаление пользователя с ID = 1:
```sql
DELETE FROM FriendshipStatus
WHERE user_id = 1;
DELETE FROM FriendshipStatus 
WHERE friend_id = 1;
DELETE FROM FilmUserLikes
WHERE user_id = 1;
DELETE FROM Users 
WHERE user_id = 1;
```

12. Получить пользователя с ID = 1
```sql
SELECT u.id u_id,
       u.email u_email,
       u.login u_login,
       u.name u_name,
       u.birthday u_bd,
       uu.id f_id,
       uu.email f_email,
       uu.login f_login,
       uu.name f_name,
       uu.birthday f_bd
FROM Users u
         LEFT JOIN FriendshipStatus fs on u.id = fs.user_id
         LEFT JOIN Users uu on.uu.id = fs.friend_id
WHERE u.id = 1;
```