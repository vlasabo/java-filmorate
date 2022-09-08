package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.MyValidationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "users")
public class UserController {
	private final HashMap<Integer, User> allUsers = new HashMap<>();

	@PutMapping
	public User updateUser(@Valid @RequestBody User user) {
		setName(user);
		int id = user.getId();

		if (user.getLogin().contains(" ")) {
			log.debug("incorrect update user {}", user);
			throw new MyValidationException("incorrect login");
		}

		if (allUsers.containsKey(id)) {
			allUsers.put(id, user);
			log.debug("correct update user {}", user);
		} else {
			log.debug("incorrect update user {}", user);
			throw new NotFoundException("no user with this id");
		}
		return user;
	}

	@PostMapping
	public User addUser(@Valid @RequestBody User user) {
		setName(user);
		if (user.getLogin().contains(" ")) {
			log.debug("incorrect adding user {}", user);
			throw new MyValidationException("incorrect login");
		} else {
			user.setId(allUsers.size() + 1);
			allUsers.put(allUsers.size() + 1, user); //new user have id=0?
			log.debug("correct adding user {}", user);
		}
		return user;
	}

	@GetMapping
	public List<User> getAllUsers() {
		log.debug("get all users");
		return new ArrayList<>(allUsers.values());
	}

	private void setName(User user) {
		if (user.getName() == null || user.getName().isBlank()) {
			user.setName(user.getLogin());
		}
	}

}
