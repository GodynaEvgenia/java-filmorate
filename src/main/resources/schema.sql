  CREATE TABLE IF NOT EXISTS genre(
      id INT AUTO_INCREMENT PRIMARY KEY,
      name VARCHAR(50) NOT NULL,
      description VARCHAR(255)
  );

  CREATE TABLE IF NOT EXISTS rating(
      id integer AUTO_INCREMENT PRIMARY KEY,
      name VARCHAR(50) NOT NULL,
      description VARCHAR(255)
  );

  CREATE TABLE IF NOT EXISTS films (
      id INT AUTO_INCREMENT PRIMARY KEY,
      name VARCHAR(255) NOT NULL,
      description VARCHAR(255),
      release_date DATE,
      duration INT,
      rating INT REFERENCES rating
  );

  CREATE TABLE IF NOT EXISTS users(
      id INT AUTO_INCREMENT PRIMARY KEY,
      name VARCHAR(100) NOT NULL,
      login VARCHAR(50) NOT NULL,
      email VARCHAR(100) NOT NULL,
      birthday DATE
  );

  CREATE TABLE IF NOT EXISTS likes (
      id INT AUTO_INCREMENT PRIMARY KEY,
      user_id INT NOT NULL REFERENCES users,
      film_id INT NOT NULL REFERENCES films
  );

  CREATE TABLE IF NOT EXISTS friendship(
      id INT AUTO_INCREMENT PRIMARY KEY,
      user_id INT NOT NULL REFERENCES users,
      friend_id INT NOT NULL REFERENCES users,
      status VARCHAR(50)
  );

  CREATE TABLE IF NOT EXISTS film_genre(
      id INT AUTO_INCREMENT PRIMARY KEY,
      film_id INT NOT NULL REFERENCES films,
      genre_id INT NOT NULL REFERENCES genre
  );