MERGE INTO mpa (id, name) KEY (id) VALUES
(1, 'G'),
(2, 'PG'),
(3, 'PG-13'),
(4, 'R'),
(5, 'NC-17');

MERGE INTO genres (id, name) KEY (id) VALUES
(1, 'Комедия'),
(2, 'Драма'),
(3, 'Триллер'),
(4, 'Мультфильм'),
(5, 'Боевик'),
(6, 'Фантастика');
