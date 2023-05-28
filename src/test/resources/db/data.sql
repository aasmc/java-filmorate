INSERT INTO Genres (name) SELECT 'Комедия' WHERE NOT EXISTS (SELECT name FROM Genres WHERE name = 'Комедия');
INSERT INTO Genres (name) SELECT 'Драма' WHERE NOT EXISTS (SELECT name FROM Genres WHERE name = 'Драма');
INSERT INTO Genres (name) SELECT 'Мультфильм' WHERE NOT EXISTS (SELECT name FROM Genres WHERE name = 'Мультфильм');
INSERT INTO Genres (name) SELECT 'Триллер' WHERE NOT EXISTS (SELECT name FROM Genres WHERE name = 'Триллер');
INSERT INTO Genres (name) SELECT 'Документальный' WHERE NOT EXISTS (SELECT name FROM Genres WHERE name = 'Документальный');
INSERT INTO Genres (name) SELECT 'Боевик' WHERE NOT EXISTS (SELECT name FROM Genres WHERE name = 'Боевик');

INSERT INTO Ratings (name) SELECT 'G' WHERE NOT EXISTS (SELECT name FROM Ratings WHERE name = 'G');
INSERT INTO Ratings (name) SELECT 'PG' WHERE NOT EXISTS (SELECT name FROM Ratings WHERE name = 'PG');
INSERT INTO Ratings (name) SELECT 'PG-13' WHERE NOT EXISTS (SELECT name FROM Ratings WHERE name = 'PG-13');
INSERT INTO Ratings (name) SELECT 'R' WHERE NOT EXISTS (SELECT name FROM Ratings WHERE name = 'R');
INSERT INTO Ratings (name) SELECT 'NC-17' WHERE NOT EXISTS (SELECT name FROM Ratings WHERE name = 'NC-17');
