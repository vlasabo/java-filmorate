package ru.yandex.practicum.filmorate.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
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

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

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
    }

}
