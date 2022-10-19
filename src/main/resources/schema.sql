CREATE TABLE IF NOT EXISTS films(
id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
name VARCHAR,
description VARCHAR,
release_date DATE,
duration INT);

CREATE TABLE IF NOT EXISTS genres(
genre_id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
genre_name VARCHAR);

CREATE TABLE IF NOT EXISTS films_genres(
id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
film_id INTEGER REFERENCES films(id) ON DELETE CASCADE,
genre_id INTEGER REFERENCES genres(genre_id) ON DELETE CASCADE);

CREATE TABLE IF NOT EXISTS mpa(
id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
mpa_name VARCHAR);

CREATE TABLE IF NOT EXISTS films_mpa(
film_id INTEGER PRIMARY KEY,
FOREIGN KEY (film_id) REFERENCES films(id),
mpa_id INTEGER REFERENCES mpa(id));

CREATE TABLE IF NOT EXISTS users(
id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
email VARCHAR,
login VARCHAR NOT NULL,
name VARCHAR,
birthday DATE);

CREATE TABLE IF NOT EXISTS likes_film(
id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
film_id INTEGER REFERENCES films(id),
user_id INTEGER REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS users_friendship(
id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
user1_id INTEGER REFERENCES users(id),
user2_id INTEGER REFERENCES users(id),
mutually boolean
);

--INSERT INTO users (id, email,login,name)
--VALUES (1,'aa@bb.com', '11111login11', 'vladimir');