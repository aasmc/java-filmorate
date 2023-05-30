drop table if exists Genres cascade;
drop table if exists Ratings cascade;
drop table if exists Films cascade;
drop table if exists FilmGenre cascade;
drop table if exists Users cascade;
drop table if exists FilmUserLikes cascade;
drop table if exists FriendshipStatus cascade;

create table if not exists Genres
(
    ID   BIGINT auto_increment primary key,
    NAME VARCHAR(15) not null
);

create table if not exists Ratings
(
    ID   BIGINT auto_increment primary key,
    NAME VARCHAR(15) not null
);

create table if not exists Films
(
    ID           BIGINT auto_increment primary key,
    NAME         VARCHAR(63) not null,
    DESCRIPTION  VARCHAR(255) not null,
    RELEASE_DATE TIMESTAMP         not null,
    DURATION     BIGINT            not null,
    RATING_ID    BIGINT,
    constraint "films_ratings_id_fk"
        foreign key (RATING_ID) references Ratings
);

create table if not exists FilmGenre
(
    FILM_ID  BIGINT not null,
    GENRE_ID BIGINT not null,
    constraint "film_genre_pk"
        primary key (FILM_ID, GENRE_ID),
    constraint "film_genre_films_id_fk"
        foreign key (FILM_ID) references Films,
    constraint "film_genre_genres_id_fk"
        foreign key (GENRE_ID) references Genres
);

create table if not exists Users
(
    ID       BIGINT auto_increment primary key,
    EMAIL    VARCHAR(63) not null,
    LOGIN    VARCHAR(63) not null,
    NAME     VARCHAR(63) not null,
    BIRTHDAY TIMESTAMP         not null
);

create table if not exists FilmUserLikes
(
    FILM_ID BIGINT not null,
    USER_ID BIGINT not null,
    constraint "film_user_likes_pk"
        primary key (FILM_ID, USER_ID),
    constraint "film_user_likes_film_id_fk"
        foreign key (FILM_ID) references Films,
    constraint "film_user_likes_user_id_fk"
        foreign key (USER_ID) references Users
);

create table if not exists FriendshipStatus
(
    USER_ID   BIGINT not null,
    FRIEND_ID BIGINT not null,
    constraint "friendship_status_pk"
        primary key (USER_ID, FRIEND_ID),
    constraint "friendship_status_user_id_friend_fk"
        foreign key (FRIEND_ID) references Users,
    constraint "friendship_status_user_id_user_fk"
        foreign key (USER_ID) references Users,
    constraint "friends_are_distinct_ck"
        check (USER_ID <> FRIEND_ID)
);
