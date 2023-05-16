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
и user_id не могут быть одинаковыми, для этого при создании таблиц будет добавлен CHECK constraint.
Поле статус может содержать следующие значения: PENDING, ACCEPTED, REJECTED.  

### Пример:

1. Пользователь с ID 1 добавляет в друзья пользователя с ID 2 -> в БД появляется запись:
```text
user_id   friend_id     status
   1         2          PENDING
```
2. Пользователь с ID 2 принимает приглашение -> в БД появляются две записи:
```text
user_id   friend_id     status
   1         2          ACCEPTED
   2         1          ACCEPTED
```
Такой вариант позволяет упростить написание запросов на поиск друзей.

3. Пользователь с ID 2 не разрешает пользователю 1 заявку в друзья
```text
user_id   friend_id     status
   1         2          REJECTED
```

4. При удалении друга, если заявка ранее была одобрена, то удаляются обе записи, если заявка была
в статусе PENDING или REJECTED, то и удалять придется только одну запись.
5. При удалении пользователя из системы, необходимо в рамках одной транзакции сначала удалить 
записи о друзьях, а после этого удалять запись о пользователе из таблицы БД.

### Прмеры запросов
1. Получить все фильмы:
```sql
SELECT f.id,
       f.name,
       f.description,
       f.release_date,
       f.duration,
       g.name
FROM Films f LEFT JOIN FilmGenre fg ON f.id = fg.film_id
LEFT JOIN Genres g ON g.id = fg.genre_id;
```

2. Добавить лайк фильму с ID = 1, от пользователя с ID = 2
```sql
INSERT INTO FilmUserLikes (film_id, user_id) VALUES (1, 2);
```

3. Удалить у фильма с ID = 1 лайк пользователя с ID = 2
```sql
DELETE FROM FilmUserLikes WHERE film_id = 1 ANF user_id = 2;
```
4. Получить 10 самых популярных фильмов:
```sql
WITH top_id_to_count AS (SELECT ff.id t_id, COUNT(ful.film_id) cnt
                         FROM Films ff
                         LEFT JOIN FilmUserLikes ful ON ff.id = ful.film_id
                         GROUP BY t_id
                         ORDER BY cnt DESC
                         LIMIT 10)
SELECT f.id,
       f.name,
       f.description,
       f.release_date,
       f.duration,
       g.name
    FROM Films f 
    LEFT JOIN FilmGenre fg ON f.id = fg.film_id
    LEFT JOIN Genres g ON g.id = fg.genre_id
    WHERE f.id IN (SELECT t_id FROM top_id_to_count);
```
То же самое, но без жанров и в отсортированном по просмотрам порядке: 
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
FROM "Films" f
    INNER JOIN top_id_to_count titc on f.id = titc.t_id
    ORDER BY titc.cnt DESC;
```
5. Получить всех пользователей
```sql
SELECT * FROM Users
```
6. Получить друзей пользователя с ID = 1
```sql
SELECT *
FROM users u
WHERE u.id IN
      (SELECT f.friend_id
       FROM users uu
       INNER JOIN FriendshipStatus f ON uu.id = f.user_id
       WHERE f.status = 'ACCEPTED'
       AND uu.id = 1);
```
7. Получить общих друзей пользователя с ID = 1 и пользователя с ID = 2
```sql
WITH common_friend_ids AS (
    SELECT f.friend_id f_id
    FROM users u
    INNER JOIN FriendshipStatus f ON u.id = f.user_id
    WHERE f.status = 'ACCEPTED'
    AND u.id = 1
    INTERSECT
    SELECT ff.friend_id f_id
    FROM users uu
    INNER JOIN FriendshipStatus ff ON uu.id = ff.user_id
    WHERE ff.status = 'ACCEPTED'
    AND uu.id = 2
)
SELECT *
FROM Users 
WHERE id IN (SELECT f_id FROM common_friend_ids); 
```
8. Пользователь с ID = 1 предлагает пользователю с ID = 2 дружбу:
```sql
INSERT INTO FriendshipStatus (user_id, friend_id, status)
VALUES (1, 2, 'PENDING');
```
9. Пользователь с ID = 2 одобряет заявку пользователя с ID = 1:
```sql
UPDATE FriendshipStatus SET status = 'ACCEPTED'
WHERE user_id = 1 AND friend_id = 2;
INSERT INTO FriendshipStatus (user_id, friend_id, status)
VALUES (2, 1, 'ACCEPTED');
```
10. Пользователь с ID = 1 удаляет из друзей пользователя с ID = 2:
```sql
DELETE FROM FriendshipStatus 
WHERE user_id = 1 AND friend_id = 2;
DELETE FROM FriendshipStatus
WHERE user_id = 2 AND friend_id = 1;
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