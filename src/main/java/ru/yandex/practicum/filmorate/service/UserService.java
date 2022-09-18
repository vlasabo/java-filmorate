package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
        if (userOptional.isPresent()) {
            return userOptional.get();
        } else {
            log.debug("User by id {} not found", userId);
            throw new NotFoundException(String.format("User by id %d not found", userId));
        }

    }

    public User addFriend(int userId, int friendId, boolean add) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);
        if (add) {
            user.addFriend(friendId);
            friend.addFriend(userId);
        } else {
            user.removeFriend(friendId);
            friend.removeFriend(userId);
        }
        return user;
    }

    public List<User> getAllFriends(int userId) {
        User user = getUserById(userId);
        Set<Integer> allFriendsId = user.getFriends();
        return allFriendsId.stream().map(this::getUserById).collect(Collectors.toList());
    }

    public List<User> getIntersectionFriends(int userId, int otherId) {
        getUserById(userId); //check
        getUserById(otherId); //check
        var listFriendsOtherUser = getAllFriends(otherId);
        return getAllFriends(userId).stream().filter(listFriendsOtherUser::contains).collect(Collectors.toList());
    }
}
