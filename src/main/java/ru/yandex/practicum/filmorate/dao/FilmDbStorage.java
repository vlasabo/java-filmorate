package ru.yandex.practicum.filmorate.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.film_attributes.Genre;
import ru.yandex.practicum.filmorate.model.film_attributes.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Qualifier("filmDbStorage")
@Slf4j
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    private UserStorage userStorage;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final String SQL_FIND_ALL_GENRES = "SELECT * FROM genres";
    private static final String SQL_FIND_ALL_MPA = "SELECT * FROM mpa";
    private static final String SQL_FIND_ALL_FILMS = "SELECT * FROM films";
    private static final String SQL_INSERT_NEW_FILM =
            "insert into films (name, description, release_date, duration) values (?, ?, ?, ?)";
    private static final String SQL_UPDATE_FILM =
            "merge into films (id, name, description, release_date, duration) values (?, ?, ?, ?, ?)";

    private static final String SQL_FIND_ALL_GENRES_BY_FILM_ID =
            "SELECT genre_id FROM films_genres WHERE film_id = ?";

    private static final String SQL_FIND_MPA_BY_FILM_ID =
            "SELECT mpa_id  FROM films_mpa WHERE film_id = ?";
    private static final String SQL_DELETE_FILM_MPA =
            "DELETE FROM films_mpa WHERE film_id = ?";
    private static final String SQL_ADD_FILM_MPA =
            "INSERT INTO films_mpa (film_id, mpa_id) VALUES (?, ?)";
    private static final String SQL_DELETE_FILM_GENRES =
            "DELETE FROM films_genres WHERE film_id = ?";
    private static final String SQL_ADD_FILM_GENRES =
            "INSERT INTO films_genres (film_id, genre_id) VALUES (?, ?)";

    private static final String SQL_ADD_FILM_LIKE =
            "INSERT INTO likes_film (film_id, user_id) VALUES (?, ?)";

    private static final String SQL_REMOVE_FILM_LIKE =
            "DELETE FROM likes_film WHERE (film_id= ? AND user_id= ?) ";

    @Override
    public Film addFilm(Film film) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement pst = connection.prepareStatement(SQL_INSERT_NEW_FILM, Statement.RETURN_GENERATED_KEYS);
            pst.setString(1, film.getName());
            pst.setString(2, film.getDescription());
            pst.setDate(3, Date.valueOf(film.getReleaseDate()));
            pst.setInt(4, film.getDuration());
            return pst;
        }, keyHolder);
        int id = keyHolder.getKey().intValue();
        film.setId(id);
        film.setMpa(getMpaById(film.getMpa().getId())); //in Java object
        updateMpaForFilmInDb(film.getId(), film.getMpa().getId()); //in DB

        var newGenresList = film.getGenres().stream().distinct()
                .map(g -> getGenreById(g.getId())).collect(Collectors.toList());
        film.setGenres(new ArrayList<>(newGenresList)); //in Java object
        updateGenresForFilmInDb(film.getId(), newGenresList.stream().distinct().map(genre -> //in DB
                genre.getId()).collect(Collectors.toList()));
        log.debug("correct adding film {}", film);

        return film;
    }

    @Override
    public List<Film> getAllFilms() {
        ArrayList<Film> allFilms = new ArrayList<>();
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet(SQL_FIND_ALL_FILMS);
        while (filmRows.next()) {
            allFilms.add(getFilmFromRow(filmRows));
        }
        return allFilms;
    }

    @Override
    public Optional<Film> getFilmById(int filmId) {
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet(SQL_FIND_ALL_FILMS + " where id = ?", filmId);
        if (filmRows.next()) {
            Film film = getFilmFromRow(filmRows);
            return Optional.of(film);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Film updateFilm(Film film) {
        if (getFilmById(film.getId()).isPresent()) {
            jdbcTemplate.update(SQL_UPDATE_FILM, film.getId(), film.getName(),
                    film.getDescription(), Date.valueOf(film.getReleaseDate()), film.getDuration());
            film.setMpa(getMpaById(film.getMpa().getId())); //in Java object
            updateMpaForFilmInDb(film.getId(), film.getMpa().getId()); //in DB

            var newGenresList = film.getGenres().stream().distinct()
                    .map(g -> getGenreById(g.getId())).collect(Collectors.toList());
            film.setGenres(new ArrayList<>(newGenresList)); //in Java object
            updateGenresForFilmInDb(film.getId(), newGenresList.stream().distinct().map(genre ->
                    genre.getId()).collect(Collectors.toList()));
            log.debug("correct update film {}", film);
        } else {
            log.debug("incorrect update film {}", film);
            throw new NotFoundException("no film with this id");
        }
        return film;
    }

    @Override
    public List<Genre> getAllGenres() {
        List<Genre> allGenres = new ArrayList<>();
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(SQL_FIND_ALL_GENRES);
        while (userRows.next()) {
            Genre genre = new Genre(userRows.getString("genre_name"), userRows.getInt("genre_id"));
            allGenres.add(genre);
        }
        return allGenres;
    }

    @Override
    public Genre getGenreById(int genreid) {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(SQL_FIND_ALL_GENRES + " where genre_id=?", genreid);
        if (userRows.next()) {
            return new Genre(userRows.getString("genre_name"), userRows.getInt("genre_id"));
        } else {
            throw new NotFoundException("incorrect genre id!");
        }
    }

    @Override
    public List<Mpa> getAllMpa() {
        List<Mpa> allMpa = new ArrayList<>();
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(SQL_FIND_ALL_MPA);
        while (userRows.next()) {
            Mpa mpa = new Mpa(userRows.getString("mpa_name"), userRows.getInt("id"));
            allMpa.add(mpa);
        }
        return allMpa;
    }

    @Override
    public Mpa getMpaById(Integer mpaId) {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(SQL_FIND_ALL_MPA + " where id=?", mpaId);
        if (userRows.next()) {
            return new Mpa(userRows.getString("mpa_name"), userRows.getInt("id"));
        } else {
            throw new NotFoundException("incorrect MPA id!");
        }
    }

    @Override
    public void addLike(int filmId, int userId) {
        removeLike(filmId, userId);
        jdbcTemplate.update(SQL_ADD_FILM_LIKE, filmId, userId);
    }

    @Override
    public void removeLike(int filmId, int userId) {
        jdbcTemplate.update(SQL_REMOVE_FILM_LIKE, filmId, userId);
    }

    @Override
    public List<Film> topNFilms(int count) {
        List<Film> topFilms = new ArrayList<>();
        SqlRowSet likesRows = jdbcTemplate.queryForRowSet("SELECT FILM_ID  FROM LIKES_FILM lf GROUP BY FILM_ID " +
                "ORDER BY COUNT( USER_ID) DESC LIMIT ?", count);
        while (likesRows.next()) {
            Optional<Film> filmOptional = getFilmById(likesRows.getInt("film_id"));
            if (filmOptional.isPresent()) {
                topFilms.add(filmOptional.get());
            }
        }
        return topFilms;
    }

    public List<Film> searchNFilmsByString(String str, int count) {
        String SQL_SEARCH_FILMS_BY_STRING ="SELECT ff.* " +
                "FROM (SELECT * FROM films AS f WHERE LOWER(f.name) LIKE ?) AS ff " +
                "LEFT OUTER JOIN likes_film AS lf ON ff.id = lf.film_id " +
                "GROUP BY ff.id ORDER BY COUNT(lf.user_id) DESC LIMIT ? ";

        SqlRowSet filmRows = jdbcTemplate.queryForRowSet(SQL_SEARCH_FILMS_BY_STRING
                , "%" + str + "%"
                , count);
        List<Film> foundFilms = new ArrayList<>();
        while (filmRows.next()) {
            foundFilms.add(getFilmFromRow(filmRows));
        }
        return foundFilms;
    }

    private Set<Integer> getSetLikesForFilmFromDb(int filmId) {
        Set<Integer> likes = new HashSet<>();
        SqlRowSet likesRows = jdbcTemplate.queryForRowSet("SELECT user_id FROM likes_film WHERE film_id = ?", filmId);
        while (likesRows.next()) {
            likes.add(likesRows.getInt("user_id"));
        }
        return likes;
    }

    private Film getFilmFromRow(SqlRowSet filmRows) {
        Film film = new Film(filmRows.getString("name"), filmRows.getString("description")
                , filmRows.getDate("release_date").toLocalDate(), filmRows.getInt("duration"));
        film.setId(filmRows.getInt("id"));
        film.setGenres(getGenresForFilmId(film.getId()));
        film.setMpa(getMpaForFilmId(film.getId()));
        film.setLikes(getSetLikesForFilmFromDb(film.getId()));
        return film;
    }

    private ArrayList<Genre> getGenresForFilmId(int filmId) {
        var allGenresForFilmList = new ArrayList<Genre>();
        SqlRowSet genresIntRow = jdbcTemplate.queryForRowSet(SQL_FIND_ALL_GENRES_BY_FILM_ID, filmId);
        while (genresIntRow.next()) {
            Genre genre = getGenreById(genresIntRow.getInt("genre_id"));
            allGenresForFilmList.add(genre);
        }
        return allGenresForFilmList;
    }

    private Mpa getMpaForFilmId(int filmId) {

        SqlRowSet mpaIntRow = jdbcTemplate.queryForRowSet(SQL_FIND_MPA_BY_FILM_ID, filmId);
        mpaIntRow.next();
        return getMpaById(mpaIntRow.getInt("mpa_id"));
    }

    private void updateGenresForFilmInDb(int filmId, List<Integer> genresId) {
        jdbcTemplate.update(SQL_DELETE_FILM_GENRES, filmId);
        if (genresId.size() == 0) {
            return;
        }

        var allGenresList = getAllGenres();
        var necessaryGenresIntList = allGenresList.stream().filter(g -> genresId.contains(g.getId()))
                .map(g -> g.getId()).collect(Collectors.toList());
        jdbcTemplate.batchUpdate("INSERT INTO films_genres (film_id, genre_id) VALUES (?, ?)",
                new BatchPreparedStatementSetter() {
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setInt(1, filmId);
                        ps.setInt(2, necessaryGenresIntList.get(i));
                    }

                    public int getBatchSize() {
                        return necessaryGenresIntList.size();
                    }
                });
    }

    private void updateMpaForFilmInDb(int filmId, int filmMpaId) {
        jdbcTemplate.update(SQL_DELETE_FILM_MPA, filmId);
        jdbcTemplate.update(SQL_ADD_FILM_MPA, filmId, filmMpaId);
    }


}

