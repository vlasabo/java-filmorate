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
import ru.yandex.practicum.filmorate.model.Director;
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
import java.util.function.Function;
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
    private static final String SQL_ADD_FILM_GENRES = //TODO: удалить
            "INSERT INTO films_genres (film_id, genre_id) VALUES (?, ?)";

    private static final String SQL_ADD_FILM_LIKE =
            "INSERT INTO likes_film (film_id, user_id) VALUES (?, ?)";

    private static final String SQL_REMOVE_FILM_LIKE =
            "DELETE FROM likes_film WHERE (film_id= ? AND user_id= ?) ";

    private static final String SQL_REMOVE_FILM_BY_ID =
            "DELETE FROM films WHERE id = ?";

    private static final String SQL_FIND_ALL_LIKED_FILMS = "SELECT film_id FROM likes_film WHERE user_id = ?";


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
        updateDirectorsForFilmInDb(film);
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

        fillDirectorsFromDb(allFilms);

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
            updateDirectorsForFilmInDb(film);
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

        fillDirectorsFromDb(topFilms);

        return topFilms;
    }

    @Override
    public List<Film> searchFilmsByString(String query, String searchBy) {
        SqlRowSet filmRows = null;
        SqlRowSet filmRows2 = null;

        log.info("SearchFilms " + searchBy);

        String lquery = query.toLowerCase();

        switch (searchBy) {
            case "title":
                filmRows = searchFilmsByTitle(lquery);
                break;
            case "director":
                log.info("SearchFilms: Director is  Not implemented");
                filmRows = searchFilmsByDirector(lquery);
                break;
            case "both":
                log.info("SearchFilms: Both is Not implemented");
//                filmRows = searchFilmsByDirTitle(lquery);
                filmRows = searchFilmsByTitle(lquery);
                filmRows2 = searchFilmsByDirector(lquery);
                break;
            default:
                throw new RuntimeException("Invalid argument searchBy: " + searchBy);
        }

        List<Film> foundFilms = new ArrayList<>();

        if (filmRows2 != null) {
            while (filmRows2.next()) {
                Film film = getFilmFromRow(filmRows2);
                Director director = getDirectorForFilmId(film.getId());
                if (director != null) film.getDirectors().add(director);
                foundFilms.add(film);
            }
        }


        while (filmRows.next()) {
            Film film = getFilmFromRow(filmRows);
            Director director = getDirectorForFilmId(film.getId());
            if (director != null) film.getDirectors().add(director);
            foundFilms.add(film);
        }
        return foundFilms;
    }

    private SqlRowSet searchFilmsByTitle(String query) {
        String SQL_SEARCH_BY_TITLE = "SELECT f.* " +
                "FROM (SELECT * FROM films AS fs WHERE LOWER(fs.name) LIKE ?) AS f " +
                "LEFT OUTER JOIN likes_film AS lf ON f.id = lf.film_id " +
                "GROUP BY f.id ORDER BY COUNT(lf.user_id) DESC";

        return jdbcTemplate.queryForRowSet(SQL_SEARCH_BY_TITLE, "%" + query + "%");
    }

    private SqlRowSet searchFilmsByDirector(String query) {

//        throw new RuntimeException("SearchFilms: Director is Not implemented");
        String SQL_SEARCH_BY_DIRECTOR = "SELECT f.* " +
                "FROM (SELECT * FROM directors AS ds WHERE LOWER(ds.name) LIKE ?) AS d " +
                "INNER JOIN films_directors AS df ON d.id = df.director_id " +
                "INNER JOIN films AS f ON df.film_id = f.id " +
                "LEFT OUTER JOIN likes_film AS lf ON f.id = lf.film_id " +
                "GROUP BY f.id ORDER BY COUNT(lf.user_id) DESC";

        return jdbcTemplate.queryForRowSet(SQL_SEARCH_BY_DIRECTOR, "%" + query + "%");
    }

    private SqlRowSet searchFilmsByDirTitle(String query) {
//        throw new RuntimeException("SearchFilms: Both is Not implemented");
        String SQL_SEARCH_BY_DIR_TITLE = "SELECT f.* " +
                "FROM (SELECT * FROM directors AS ds WHERE LOWER(ds.name) LIKE ?) AS d " +
                "RIGHT OUTER JOIN films_directors AS df ON d.id = df.director_id " +
                "RIGHT OUTER JOIN(SELECT * FROM films AS fs WHERE LOWER(fs.name) LIKE ?) " +
                "AS f ON df.film_id = f.id " +
                "LEFT OUTER JOIN likes_film AS lf ON f.id = lf.film_id " +
                "GROUP BY f.id ORDER BY COUNT(lf.user_id) DESC";

        return jdbcTemplate.queryForRowSet(SQL_SEARCH_BY_DIR_TITLE
                , "%" + query + "%"
                , "%" + query + "%");
    }


    public void deleteFilm(int id) {
        jdbcTemplate.update(SQL_REMOVE_FILM_BY_ID, id);
    }
    @Override
    public List<Film> getAllFilmsUserLiked(int userId) {
        List<Film> allFilms = new ArrayList<>();
        List<Integer> allFilmsId = new ArrayList<>();
        SqlRowSet filmIdRows = jdbcTemplate.queryForRowSet(SQL_FIND_ALL_LIKED_FILMS, userId);
        while (filmIdRows.next()) {
            allFilmsId.add(filmIdRows.getInt("film_id"));
        }
        return getListFilmsByListId(allFilmsId);
    }

    @Override
    public List<Film> getFilmByDirector(int directorId, String sortBy) {

        if (Objects.isNull(sortBy)){
            sortBy = "";
        }

        String sql =    "SELECT " +
                        "   F.ID, " +
                        "   F.NAME, " +
                        "   F.DESCRIPTION, " +
                        "   F.DURATION, " +
                        "   F.RELEASE_DATE, " +
                        "   COUNT(DISTINCT LF.USER_ID) likes " +
                        "FROM FILMS_DIRECTORS FD " +
                        "    INNER JOIN FILMS F on F.ID = FD.FILM_ID " +
                        "    LEFT JOIN LIKES_FILM LF on F.ID = LF.FILM_ID " +
                        "WHERE DIRECTOR_ID = ? " +
                        "GROUP BY F.ID ";

        switch (sortBy){
            case "likes":
                sql = sql.concat("ORDER BY likes");
                break;
            case "year":
                sql = sql.concat("ORDER BY F.RELEASE_DATE");
                break;
        }

        List<Film> films = new ArrayList<>();
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql,
                directorId);
        while (rowSet.next()) {
            films.add(getFilmFromRow(rowSet));
        }
        if (films.size() == 0){
            log.debug("Director id={} not found", directorId);
            throw new NotFoundException("Director id=" + directorId + " not found");
        }
        fillDirectorsFromDb(films);

        return films;
    }

    private List<Film> getListFilmsByListId(List<Integer> ids) {
        List<Film> allFilms = new ArrayList<>();
        String stringListFilmsId = "(" + ids.toString().substring(1, ids.toString().length() - 1).replace(" ", "") + ")";
        SqlRowSet likeRows = jdbcTemplate.queryForRowSet("SELECT * FROM likes_film WHERE film_id IN " + stringListFilmsId);
        SqlRowSet genreRows = jdbcTemplate.queryForRowSet("SELECT * FROM films_genres WHERE film_id IN " + stringListFilmsId);
        SqlRowSet mpaRows = jdbcTemplate.queryForRowSet("SELECT * FROM films_mpa WHERE film_id IN " + stringListFilmsId);
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet("SELECT * FROM films WHERE id IN " + stringListFilmsId);
        SqlRowSet genres = jdbcTemplate.queryForRowSet("SELECT * FROM genres");
        SqlRowSet mpa = jdbcTemplate.queryForRowSet("SELECT * FROM mpa");

        //создадим все жанры
        List<Genre> allGenres = new ArrayList<>();
        while (genres.next()) {
            allGenres.add(new Genre(genres.getString("genre_name"), genres.getInt("genre_id")));
        }

        //создадим все mpa
        var allMpa = new HashMap<Integer, Mpa>();
        while (mpa.next()) {
            allMpa.put(mpa.getInt("id"), new Mpa(mpa.getString("mpa_name"), mpa.getInt("id")));
        }

        for (int filmId : ids) {
            filmRows.next();
            Film film = new Film(filmRows.getString("name"), filmRows.getString("description")
                    , filmRows.getDate("release_date").toLocalDate(), filmRows.getInt("duration"));
            film.setId(filmId);
            //установим лайки
            HashSet<Integer> likesSet = new HashSet<>();
            while (likeRows.next()) {
                if (("" + filmId).equals(likeRows.getString("film_id"))) {
                    likesSet.add(likeRows.getInt("user_id"));
                }
            }
            likeRows.beforeFirst();
            film.setLikes(likesSet);

            //установим жанры
            List<Integer> genresListId = new ArrayList<>();
            while (genreRows.next()) {
                if (("" + filmId).equals(genreRows.getString("film_id"))) {
                    genresListId.add(genreRows.getInt("genre_id"));
                }
            }
            genreRows.beforeFirst();

            List<Genre> genresList = allGenres.stream().filter(x -> genresListId.contains(x.getId())).collect(Collectors.toList());
            film.setGenres((ArrayList<Genre>) genresList);

            //установим MPA
            while (mpaRows.next()) {
                if (("" + filmId).equals(mpaRows.getString("film_id"))) {
                    film.setMpa(allMpa.get(mpaRows.getInt("mpa_id")));
                }
            }
            mpaRows.beforeFirst();
            allFilms.add(film);
        }

        fillDirectorsFromDb(allFilms);

        return allFilms;

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


    private Director getDirectorForFilmId(int filmId) {
        String sqlGetDirector = "SELECT d.* FROM directors AS d " +
                "INNER JOIN films_directors AS fd ON d.id = fd.director_id " +
                "INNER JOIN (SELECT * FROM films AS fs WHERE fs.id = ?) AS f ON fd.film_id = f.id";

        SqlRowSet dirRow = jdbcTemplate.queryForRowSet(sqlGetDirector, filmId);
        if (dirRow.next()) {
            return new Director(dirRow.getInt("id"), dirRow.getString("name"));
        } else {
            return null;
        }
    }

    private void updateGenresForFilmInDb(int filmId, List<Integer> genresId) {
        jdbcTemplate.update(SQL_DELETE_FILM_GENRES, filmId);
        if (genresId.size() == 0) {
            return;
        }


        jdbcTemplate.batchUpdate("INSERT INTO films_genres (film_id, genre_id) VALUES (?, ?)",
                new BatchPreparedStatementSetter() {
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setInt(1, filmId);
                        ps.setInt(2, genresId.get(i));
                    }

                    public int getBatchSize() {
                        return genresId.size();
                    }
                });
    }

    private void updateMpaForFilmInDb(int filmId, int filmMpaId) {
        jdbcTemplate.update(SQL_DELETE_FILM_MPA, filmId);
        jdbcTemplate.update(SQL_ADD_FILM_MPA, filmId, filmMpaId);
    }

    private void updateDirectorsForFilmInDb(Film film){
        jdbcTemplate.update("DELETE FROM FILMS_DIRECTORS WHERE FILM_ID = ?", film.getId());

        List<Director> directors = new ArrayList<>(film.getDirectors());
        jdbcTemplate.batchUpdate("INSERT INTO FILMS_DIRECTORS (FILM_ID, DIRECTOR_ID) VALUES ( ?1, ?2 )",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setInt(1, film.getId());
                        ps.setInt(2, directors.get(i).getId());
                    }

                    @Override
                    public int getBatchSize() {
                        return directors.size();
                    }
                });

        log.debug("Updated directors for film id={}", film.getId());
    }

    private void fillDirectorsFromDb(List<Film> films) {

        if (films.size() == 0){
            return;
        }

        List<Integer> ids = films.stream().map(Film::getId).collect(Collectors.toList());
        String inSql = String.join(",", Collections.nCopies(ids.size(), "?"));

        String sql =    String.format(  "SELECT " +
                                        "   FD.FILM_ID, " +
                                        "   D.ID, " +
                                        "   D.NAME " +
                                        "FROM FILMS_DIRECTORS FD " +
                                        "   INNER JOIN DIRECTORS D " +
                                        "       ON FD.DIRECTOR_ID = D.ID " +
                                        "WHERE FILM_ID IN (%s) " +
                                        "ORDER BY FD.FILM_ID", inSql);

        Map<Integer, Film> filmMap = films.stream()
                .collect(Collectors.toMap(Film::getId, Function.identity()));

        jdbcTemplate.query(
                sql, rs -> {
                    Film film = filmMap.get(rs.getInt("film_id"));
                    film.getDirectors().clear();

                    Director director = new Director();
                    director.setId(rs.getInt("id"));
                    director.setName(rs.getString("name"));

                    film.getDirectors().add(director);
                }
                , ids.toArray());

        log.debug("Completed directors for films");
    }
}

