DROP TABLE IF EXISTS film_genre;
DROP TABLE IF EXISTS genre;
DROP TABLE IF EXISTS likes;
DROP TABLE IF EXISTS review_ratings;
DROP TABLE IF EXISTS reviews;
DROP TABLE IF EXISTS film_director;
DROP TABLE IF EXISTS films;
DROP TABLE IF EXISTS rating;
DROP TABLE IF EXISTS friendship;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS directors;

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
  CREATE TABLE directors(
        id INT AUTO_INCREMENT PRIMARY KEY,
        name VARCHAR(200) NOT NULL
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

  CREATE film_director(
       id INT AUTO_INCREMENT PRIMARY KEY,
       film_id INT NOT NULL REFERENCES films,
       director_id INT NOT NULL REFERENCES directors
   );