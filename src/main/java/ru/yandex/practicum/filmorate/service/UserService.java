package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Map;
import java.util.Optional;
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
        if (add) { //this "if" only for log
            checkMutuallyAndAddOrRemoveFriend(userId, friendId, true);
            log.debug("User {} add friend {}", userId, friendId);
        } else {
            checkMutuallyAndAddOrRemoveFriend(userId, friendId, false);
            log.debug("User {} remove friend {}", userId, friendId);
        }
        return user;
    }

    private void checkMutuallyAndAddOrRemoveFriend(int userId1, int userId2, boolean add) {
        if (add) {
            if (getUserById(userId2).getFriends().containsKey(userId1)) {
                getUserById(userId2).addFriend(userId1, true);
                getUserById(userId1).addFriend(userId2, true);
            } else {
                getUserById(userId1).addFriend(userId2, false);
            }
        } else {
            if (getUserById(userId2).getFriends().containsKey(userId1)) {
                getUserById(userId2).addFriend(userId1, false);
                getUserById(userId1).deleteFriend(userId2);
            } else {
                getUserById(userId1).deleteFriend(userId2);
            }
        }
    }

    public List<User> getAllFriends(int userId) {
        User user = getUserById(userId);
        Map<Integer, Boolean> allFriendsId = user.getFriends();
        return allFriendsId.keySet().stream().map(this::getUserById).collect(Collectors.toList());
    }

    public List<User> getIntersectionFriends(int userId, int otherId) {
        getUserById(userId); //check
        getUserById(otherId); //check
        log.debug("get friends intersections user {} and {}", userId, otherId);
        var listFriendsOtherUser = getAllFriends(otherId);
        return getAllFriends(userId).stream().filter(listFriendsOtherUser::contains).collect(Collectors.toList());
    }

    public User updateUser(User user) {
        return userStorage.updateUser(user);
    }

    public User addUser(User user) {
        return userStorage.addUser(user);
    }

    public List<User> getAllUsers() {
        log.debug("get all users");
        return userStorage.getAllUsers();
    }
}
