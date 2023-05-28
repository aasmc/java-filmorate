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
    NAME CHARACTER VARYING(20) not null
);

create table if not exists Ratings
(
    ID   BIGINT auto_increment primary key,
    NAME CHARACTER VARYING(20) not null
);

create table if not exists Films
(
    ID           BIGINT auto_increment primary key,
    NAME         CHARACTER VARYING not null,
    DESCRIPTION  CHARACTER VARYING not null,
    RELEASE_DATE TIMESTAMP         not null,
    DURATION     BIGINT            not null,
    RATING_ID    BIGINT,
    constraint "Films_Ratings_ID_fk"
        foreign key (RATING_ID) references Ratings
);

create table if not exists FilmGenre
(
    FILM_ID  BIGINT not null,
    GENRE_ID BIGINT not null,
    constraint "FilmGenre_pk"
        primary key (FILM_ID, GENRE_ID),
    constraint "FilmGenre_Films_ID_fk"
        foreign key (FILM_ID) references Films,
    constraint "FilmGenre_Genres_ID_fk"
        foreign key (GENRE_ID) references Genres
);

create table if not exists Users
(
    ID       BIGINT auto_increment primary key,
    EMAIL    CHARACTER VARYING not null,
    LOGIN    CHARACTER VARYING not null,
    NAME     CHARACTER VARYING not null,
    BIRTHDAY TIMESTAMP         not null
);

create table if not exists FilmUserLikes
(
    FILM_ID BIGINT not null,
    USER_ID BIGINT not null,
    constraint "FilmUserLikes_pk"
        primary key (FILM_ID, USER_ID),
    constraint "FilmUserLikes_Films_ID_fk"
        foreign key (FILM_ID) references Films,
    constraint "FilmUserLikes_Users_ID_fk"
        foreign key (USER_ID) references Users
);

create table if not exists FriendshipStatus
(
    USER_ID   BIGINT not null,
    FRIEND_ID BIGINT not null,
    constraint "FriendshipStatus_pk"
        primary key (USER_ID, FRIEND_ID),
    constraint "FriendshipStatus_Users_ID_Friend_fk"
        foreign key (FRIEND_ID) references Users,
    constraint "FriendshipStatus_Users_ID_User_fk"
        foreign key (USER_ID) references Users,
    constraint "FriendsAreDistinct_Check"
        check (USER_ID <> FRIEND_ID)
);
