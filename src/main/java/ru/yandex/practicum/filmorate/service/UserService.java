package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Optional;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User getUserById(int userId) {
        Optional<User> userOptional = userStorage.getAllUsers().stream().filter(u -> u.getId() == userId).findFirst();
        if (userOptional.isPresent()){
            return userOptional.get();
        } else {
            log.debug("User by id {} not found",userId);
            throw new NotFoundException(String.format("User by id %d not found",userId));
        }

    }
}
