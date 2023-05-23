INSERT INTO Genres (name) SELECT 'Комедия' WHERE NOT EXISTS (SELECT name FROM Genres WHERE name = 'Комедия');
INSERT INTO Genres (name) SELECT 'Драма' WHERE NOT EXISTS (SELECT name FROM Genres WHERE name = 'Драма');
INSERT INTO Genres (name) SELECT 'Мультфильм' WHERE NOT EXISTS (SELECT name FROM Genres WHERE name = 'Мультфильм');
INSERT INTO Genres (name) SELECT 'Триллер' WHERE NOT EXISTS (SELECT name FROM Genres WHERE name = 'Триллер');
INSERT INTO Genres (name) SELECT 'Документальный' WHERE NOT EXISTS (SELECT name FROM Genres WHERE name = 'Документальный');
INSERT INTO Genres (name) SELECT 'Боевик' WHERE NOT EXISTS (SELECT name FROM Genres WHERE name = 'Боевик');

INSERT INTO Ratings (rating) SELECT 'G' WHERE NOT EXISTS (SELECT rating FROM Ratings WHERE rating = 'G');
INSERT INTO Ratings (rating) SELECT 'PG' WHERE NOT EXISTS (SELECT rating FROM Ratings WHERE rating = 'PG');
INSERT INTO Ratings (rating) SELECT 'PG-13' WHERE NOT EXISTS (SELECT rating FROM Ratings WHERE rating = 'PG-13');
INSERT INTO Ratings (rating) SELECT 'R' WHERE NOT EXISTS (SELECT rating FROM Ratings WHERE rating = 'R');
INSERT INTO Ratings (rating) SELECT 'NC-17' WHERE NOT EXISTS (SELECT rating FROM Ratings WHERE rating = 'NC-17');
