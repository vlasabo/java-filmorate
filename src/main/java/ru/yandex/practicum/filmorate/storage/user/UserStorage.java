package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;


public interface UserStorage {

	User addUser(User user);

	List<User> getAllUsers();

	User updateUser(User user);

	void updateFriendship(User user1, User user2, Boolean mutually);

	Optional<User> findUserById(int userId);

	HashMap<Integer, Boolean> findAllFriends(User user);

	void removeFriends(User user, User userById);

	void deleteUser(int id);

    List<Film> getRecommendations(int userId);
}
