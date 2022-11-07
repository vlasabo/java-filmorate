package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.*;
import java.sql.Date;
import java.util.*;

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

    private static final String SQL_DELETE_USER_BY_ID = "DELETE FROM users WHERE id= ?";

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
    public void deleteUser(int id) {
        jdbcTemplate.update(SQL_DELETE_USER_BY_ID, id);
    }

    @Override
    public List<Film> getRecommendations(int userId) {
        List<Integer> rec = new ArrayList<>();
        if (findUserById(userId).isPresent()) {
            SqlRowSet resultSet = jdbcTemplate.queryForRowSet(queryRecommendations(), userId);
            while (resultSet.next()) {
                rec.add(resultSet.getInt("film_id"));
            }
            return filmStorage.getListFilmsByListId(rec);
        } else {
            throw new NotFoundException("no user with this id");
        }
    }

    private String queryRecommendations() {
                // 1. Сначала получим фильмы, которые лайкнул рассматриваемый юзер, и поместим их в таблицу REQUESTED_USER_FILMS
        return "WITH REQUESTED_USER_FILMS AS " +
                "(SELECT FL.FILM_ID FROM LIKES_FILM FL WHERE FL.USER_ID = ?1), " +
                // 3. Используя таблицы REQUESTED_USER_FILMS и NEIGHBOURS считаем количество совпадающих лайков
                // у пользователя и его ближайших соседей, оставляя 5 ближайших соседей с наибольшим количеством
                // совпадающих лайков. Помещая эти данные в таблицу COMMON_FILMS
                "COMMON_FILMS AS " +
                // 2. Определим "ближайших соседей", т.е. пользователей, лайки которых совпадают с лайками нашего юзера
                // и помещаем их в таблицу NEIGHBOURS
                "(WITH NEIGHBOURS AS " +
                "(SELECT LIKES.USER_ID FROM LIKES_FILM AS LIKES " +
                "INNER JOIN REQUESTED_USER_FILMS ON LIKES.FILM_ID = REQUESTED_USER_FILMS.FILM_ID " +
                "WHERE LIKES.USER_ID <> ?1) " +
                "SELECT NEIGHBOURS_LIKES.USER_ID, " +
                "COUNT(DISTINCT NEIGHBOURS_LIKES.FILM_ID) AS COMMON_COUNT " +
                "FROM LIKES_FILM AS NEIGHBOURS_LIKES " +
                "INNER JOIN NEIGHBOURS ON NEIGHBOURS.USER_ID = NEIGHBOURS_LIKES.USER_ID " +
                "INNER JOIN REQUESTED_USER_FILMS ON NEIGHBOURS_LIKES.FILM_ID = REQUESTED_USER_FILMS.FILM_ID " +
                "GROUP BY NEIGHBOURS_LIKES.USER_ID " +
                "ORDER BY COUNT(DISTINCT NEIGHBOURS_LIKES.FILM_ID) DESC " +
                "LIMIT 5) " +
                // колонка FILM_WEIGHT представляет собой вес фильма.
                // Считаем как сумма совпадающих лайков ближайшего соседа и нашего пользователя. То есть, чем в больше
                // количестве фильмов мнение юзера и соседа совпало, тем больший вес будет иметь оценка данного соседа по данному фильму для пользователя.
                "SELECT SUM(COMMON_FILMS.COMMON_COUNT) AS FILM_WEIGHT, " +
                "F_LIKES.FILM_ID FROM LIKES_FILM AS F_LIKES " +
                "INNER JOIN COMMON_FILMS ON F_LIKES.USER_ID = COMMON_FILMS.USER_ID " +
                "LEFT JOIN REQUESTED_USER_FILMS ON F_LIKES.FILM_ID = REQUESTED_USER_FILMS.FILM_ID " +
                "WHERE REQUESTED_USER_FILMS.FILM_ID IS NULL " +
                "GROUP BY F_LIKES.FILM_ID " +
                "ORDER BY SUM(COMMON_FILMS.COMMON_COUNT) DESC";
    }

}
