package ru.yandex.practicum.filmorate.storage.user;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final HashMap<Integer, User> allUsers = new HashMap<>();

    @Override
    public User addUser(User user) {
        setName(user);
        user.setId(allUsers.size() + 1);
        allUsers.put(allUsers.size() + 1, user);
        log.debug("correct adding user {}", user);
        return user;
    }

    @Override
    public List<User> getAllUsers() {
        log.debug("get all users");
        return new ArrayList<>(allUsers.values());
    }

    @Override
    public User updateUser(User user) {
        setName(user);
        int id = user.getId();
        if (allUsers.containsKey(id)) {
            allUsers.put(id, user);
            log.debug("correct update user {}", user);
        } else {
            log.debug("incorrect update user {}", user);
            throw new NotFoundException("no user with this id");
        }
        return user;
    }

    private void setName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}
