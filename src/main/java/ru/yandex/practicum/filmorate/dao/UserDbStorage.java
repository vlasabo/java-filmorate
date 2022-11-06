package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
@Qualifier("userDbStorage")
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;
    private static final String SQL_SELECT_ALL_FROM_USERS = "select * from users";
    private static final String SQL_INSERT_NEW_USER =
            "insert into users (email, login, name, birthday) values (?, ?, ?, ?)";
    private static final String SQL_UPDATE_USER =
            "merge into users (id, email, login, name, birthday) values (?, ?, ?, ?, ?)";
    private static final String SQL_DELETE_FRIENDSHIP =
            "DELETE FROM users_friendship WHERE (USER1_ID = ? and USER2_ID = ?) OR (USER2_ID = ? and USER1_ID = ?)";
    private static final String SQL_REMOVE_FRIEND =
            "DELETE FROM users_friendship WHERE (USER1_ID = ? and USER2_ID = ?)";
    private static final String SQL_WRITE_FRIENDSHIP =
            "insert into users_friendship (user1_id, user2_id , mutually) values (?, ?, ?)";
    private static final String SQL_FIND_ALL_FRIENDS = "SELECT * FROM users_friendship WHERE USER1_ID = ?";

    @Autowired
    private final FilmStorage filmStorage;

    @Override
    public User addUser(User user) {
        setName(user);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement pst = connection.prepareStatement(SQL_INSERT_NEW_USER, Statement.RETURN_GENERATED_KEYS);
            pst.setString(1, user.getEmail());
            pst.setString(2, user.getLogin());
            pst.setString(3, user.getName());
            pst.setDate(4, Date.valueOf(user.getBirthday()));
            return pst;
        }, keyHolder);
        int id = keyHolder.getKey().intValue();
        user.setId(id);
        log.debug("correct adding user {}", user);
        return user;

    }

    @Override
    public List<User> getAllUsers() {
        ArrayList<User> allUsers = new ArrayList<>();
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(SQL_SELECT_ALL_FROM_USERS);
        while (userRows.next()) {
            User user = new User(userRows.getString("email"), userRows.getString("login")
                    , userRows.getString("name"), userRows.getDate("birthday").toLocalDate());
            user.setId(userRows.getInt("id"));
            allUsers.add(user);
        }
        return allUsers;
    }

    @Override
    public User updateUser(User user) {
        if (findUserById(user.getId()).isPresent()) {
            setName(user);
            jdbcTemplate.update(SQL_UPDATE_USER, user.getId(), user.getEmail(),
                    user.getLogin(), user.getName(), Date.valueOf(user.getBirthday()));
            log.debug("correct update user {}", user);
        } else {
            log.debug("incorrect update user {}", user);
            throw new NotFoundException("no user with this id");
        }
        return user;
    }

    public Optional<User> findUserById(int id) {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(SQL_SELECT_ALL_FROM_USERS + " where id = ?", id);
        if (userRows.next()) {
            User user = new User(userRows.getString("email"), userRows.getString("login")
                    , userRows.getString("name"), userRows.getDate("birthday").toLocalDate());
            user.setId(userRows.getInt("id"));
            user.setFriends(findALlFriends(user));
            return Optional.of(user);
        } else {
            return Optional.empty();
        }
    }


    private void setName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    @Override
    public void updateFriendship(User user1, User user2, Boolean mutually) {
        jdbcTemplate.update(SQL_DELETE_FRIENDSHIP, user1.getId(), user2.getId(), user1.getId(), user2.getId());
        jdbcTemplate.update(SQL_WRITE_FRIENDSHIP, user1.getId(), user2.getId(), mutually);
        log.debug("correct update friendship for  users {}, {}", user1.getId(), user2.getId());
    }

    @Override
    public HashMap<Integer, Boolean> findALlFriends(User user) {
        HashMap<Integer, Boolean> friendsMap = new HashMap<>();
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(SQL_FIND_ALL_FRIENDS, user.getId());
        while (userRows.next()) {
            friendsMap.put(userRows.getInt("user2_id"), userRows.getBoolean("mutually"));
        }
        return friendsMap;
    }

    @Override
    public void removeFriends(User user1, User user2) {
        jdbcTemplate.update(SQL_REMOVE_FRIEND, user1.getId(), user2.getId());
        if (user2.getFriends().containsKey(user1.getId())) {
            updateFriendship(user2, user1, false);
        }
    }

    @Override
    public List<Film> getRecommendations(int userId) {
        /*return
                jdbcTemplate.query(
                        queryRecommendations(), preparedStatement -> {
                            preparedStatement.setInt(1, userId);
                            preparedStatement.setInt(2, userId);
                        },
                        resultSet -> {
                            List<Film> rec = new ArrayList<>();
                            while (resultSet.next()) {
                                Optional<Film> film = filmStorage.getFilmById(resultSet.getInt("film_id"));
                                film.ifPresent(rec::add);
                            }
                            return rec;
                        }
                );*/
        List<Film> recommendedFilms = new ArrayList<>();
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet(queryRecommendations().replace("?", String.valueOf(userId)));
        while (filmRows.next()) {
            Optional<Film> film = filmStorage.getFilmById(filmRows.getInt("film_id"));
            film.ifPresent(recommendedFilms::add);
        }
        return recommendedFilms;
    }

    private String queryRecommendations() {
        return "WITH REQUESTED_USER_FILMS AS " +
                "(SELECT FL.FILM_ID FROM LIKES_FILM FL WHERE FL.USER_ID = ?), " +
                "COMMON_FILMS AS " +
                "(WITH NEIGHBOURS AS " +
                "(SELECT LIKES.USER_ID FROM LIKES_FILM AS LIKES " +
                "INNER JOIN REQUESTED_USER_FILMS ON LIKES.FILM_ID = REQUESTED_USER_FILMS.FILM_ID " +
                "WHERE LIKES.USER_ID <> ?) " +
                "SELECT NEIGHBOURS_LIKES.USER_ID, " +
                "COUNT(NEIGHBOURS_LIKES.FILM_ID) AS COMMON_COUNT " +
                "FROM LIKES_FILM AS NEIGHBOURS_LIKES " +
                "INNER JOIN NEIGHBOURS ON NEIGHBOURS.USER_ID = NEIGHBOURS_LIKES.USER_ID " +
                "INNER JOIN REQUESTED_USER_FILMS ON NEIGHBOURS_LIKES.FILM_ID = REQUESTED_USER_FILMS.FILM_ID " +
                "GROUP BY NEIGHBOURS_LIKES.USER_ID " +
                "LIMIT 5) " +
                "SELECT SUM(COMMON_FILMS.COMMON_COUNT) AS FILM_WEIGHT, " +
                "F_LIKES.FILM_ID FROM LIKES_FILM AS F_LIKES " +
                "INNER JOIN COMMON_FILMS ON F_LIKES.USER_ID = COMMON_FILMS.USER_ID " +
                "LEFT JOIN REQUESTED_USER_FILMS ON F_LIKES.FILM_ID = REQUESTED_USER_FILMS.FILM_ID " +
                "WHERE REQUESTED_USER_FILMS.FILM_ID IS NULL " +
                "GROUP BY F_LIKES.FILM_ID " +
                "ORDER BY SUM(COMMON_FILMS.COMMON_COUNT) DESC";
    }

}
