-- Таблица пользователей
CREATE TABLE IF NOT EXISTS users (
    id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(254) UNIQUE NOT NULL,
    login VARCHAR(50) UNIQUE NOT NULL,
    birthday DATE
);

-- Таблица рейтингов MPA (Movie Picture Association)
CREATE TABLE IF NOT EXISTS mpa (
    id INT NOT NULL PRIMARY KEY,
    name VARCHAR(10)
);

-- Таблица фильмов
CREATE TABLE IF NOT EXISTS films (
    id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(150),
    description VARCHAR(200),
    release_date DATE NOT NULL,
    duration INT NOT NULL CHECK (duration > 0),
    mpa_id INT,
    FOREIGN KEY (mpa_id) REFERENCES mpa(id) ON DELETE SET NULL
);

-- Таблица жанров
CREATE TABLE IF NOT EXISTS genres (
    id INT NOT NULL PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);

-- Таблица режиссеров
CREATE TABLE IF NOT EXISTS directors (
    id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL
);

-- Связующая таблица для связи фильмов и режиссеров (многие‑ко‑многим)
CREATE TABLE IF NOT EXISTS film_directors (
    film_id INT REFERENCES films (id),
    director_id INT REFERENCES directors(id),
    PRIMARY KEY (film_id, director_id)
);

-- Связующая таблица для связи фильмов и жанров (многие‑ко‑многим)
CREATE TABLE IF NOT EXISTS film_genres (
    film_id INT REFERENCES films (id) ON DELETE CASCADE,
    genre_id INT REFERENCES genres(id),
    PRIMARY KEY (film_id, genre_id)
);

-- Таблица дружбы между пользователями
CREATE TABLE IF NOT EXISTS friendships (
  user_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  friend_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,  -- исправлено: убрано IS
  PRIMARY KEY (user_id, friend_id)
);

-- Таблица лайков фильмов (связь пользователей и фильмов)
CREATE TABLE IF NOT EXISTS likes (
    user_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    film_id INT NOT NULL REFERENCES films(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, film_id)
);

-- Таблица отзывов
CREATE TABLE IF NOT EXISTS reviews (
    review_id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    content VARCHAR(1000) NOT NULL,
    is_positive BOOLEAN NOT NULL,
    user_id INT NOT NULL REFERENCES users(id),
    film_id INT NOT NULL REFERENCES films(id)
);

-- Таблица реакций на отзывы (лайки/дизлайки)
CREATE TABLE IF NOT EXISTS review_reactions (
    review_id INT NOT NULL REFERENCES reviews(review_id) ON DELETE CASCADE,
    user_id INT NOT NULL REFERENCES users(id),
    is_like BOOLEAN NOT NULL,
    PRIMARY KEY (review_id, user_id)
);

-- Рейтинги MPA
MERGE INTO mpa (id, name) VALUES
    (1, 'G'),
    (2, 'PG'),
    (3, 'PG-13'),
    (4, 'R'),
    (5, 'NC-17');

-- Жанры
MERGE INTO genres (id, name) VALUES
    (1, 'Комедия'),
    (2, 'Драма'),
    (3, 'Мультфильм'),
    (4, 'Триллер'),
    (5, 'Документальный'),
    (6, 'Боевик');

-- Лента событий пользователей
CREATE TABLE IF NOT EXISTS events (
    event_id   INT         NOT NULL PRIMARY KEY AUTO_INCREMENT,
    timestamp  BIGINT      NOT NULL,
    user_id    INT         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    event_type VARCHAR(16) NOT NULL,
    operation  VARCHAR(16) NOT NULL,
    entity_id  INT         NOT NULL
);