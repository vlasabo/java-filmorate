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
    public UserService(UserStorage userDbStorage) {
        this.userStorage = userDbStorage;
    }

    public User getUserById(int userId) {
        Optional<User> userOptional = userStorage.findUserById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setFriends(userStorage.findALlFriends(user));
            return user;
        } else {
            log.debug("User by id {} not found", userId);
            throw new NotFoundException(String.format("User by id %d not found", userId));
        }

    }

    public User addFriend(int userId, int friendId, boolean add) {
        User user = getUserById(userId);
        if (add) { //this "if" only for log
            checkMutuallyAndAddOrRemoveFriend(user, friendId, true);
            log.debug("User {} add friend {}", userId, friendId);
        } else {
            checkMutuallyAndAddOrRemoveFriend(user, friendId, false);
            log.debug("User {} remove friend {}", userId, friendId);
        }
        return user;
    }

    private void checkMutuallyAndAddOrRemoveFriend(User user, int userId2, boolean add) {
        if (add) {
            if (getUserById(userId2).getFriends().containsKey(user.getId())) {
                getUserById(userId2).addFriend(user.getId(), true);
                user.addFriend(userId2, true);
                userStorage.updateFriendship(getUserById(userId2), user, true);
            } else {
                user.addFriend(userId2, false);
                userStorage.updateFriendship(user, getUserById(userId2), false);
            }
        } else {
            if (getUserById(userId2).getFriends().containsKey(user.getId())) {
                getUserById(userId2).addFriend(user.getId(), false);
                userStorage.updateFriendship(user, getUserById(userId2), false);
            }
            user.deleteFriend(userId2);
            userStorage.removeFriends(user, getUserById(userId2));
        }
    }

    public List<User> getAllFriends(int userId) {
        User user = getUserById(userId);
        Map<Integer, Boolean> allFriendsId = userStorage.findALlFriends(user);
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
