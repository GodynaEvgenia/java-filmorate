DROP TABLE IF EXISTS FILM_GENRE;

DROP TABLE IF EXISTS FRIENDSHIP;

DROP TABLE IF EXISTS GENRE;

DROP TABLE IF EXISTS LIKES;

DROP TABLE IF EXISTS REVIEW_RATINGS;

DROP TABLE IF EXISTS REVIEWS;

DROP TABLE IF EXISTS FILMS;

DROP TABLE IF EXISTS RATING;

DROP TABLE IF EXISTS USERS;

CREATE TABLE genre
(
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(50) NOT NULL,
    description VARCHAR(255)
);

CREATE TABLE rating
(
    id          integer AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(50) NOT NULL,
    description VARCHAR(255)
);

CREATE TABLE films
(
    id           INT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(255) NOT NULL,
    description  VARCHAR(255),
    release_date DATE,
    duration     INT,
    rating       INT REFERENCES rating
);

CREATE TABLE users
(
    id       INT AUTO_INCREMENT PRIMARY KEY,
    name     VARCHAR(100) NOT NULL,
    login    VARCHAR(50)  NOT NULL,
    email    VARCHAR(100) NOT NULL,
    birthday DATE
);

CREATE TABLE likes
(
    id      INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users,
    film_id INT NOT NULL REFERENCES films
);

CREATE TABLE friendship
(
    id        INT AUTO_INCREMENT PRIMARY KEY,
    user_id   INT NOT NULL REFERENCES users,
    friend_id INT NOT NULL REFERENCES users,
    status    VARCHAR(50)
);

CREATE TABLE film_genre
(
    id       INT AUTO_INCREMENT PRIMARY KEY,
    film_id  INT NOT NULL REFERENCES films,
    genre_id INT NOT NULL REFERENCES genre
);

CREATE TABLE reviews
(
    review_id   INT AUTO_INCREMENT PRIMARY KEY,
    content     VARCHAR,
    is_positive BOOLEAN,
    film_id     INT NOT NULL REFERENCES films,
    user_id     INT NOT NULL REFERENCES users,
    useful      INT
);

CREATE TABLE review_ratings
(
    review_id   INT     NOT NULL REFERENCES reviews,
    user_id     INT     NOT NULL REFERENCES users,
    is_positive boolean NOT NULL,
    PRIMARY KEY (user_id, review_id)
);