package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;
import javax.validation.ValidationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "user")
public class UserController {
	private final HashMap<Integer, User> allUsers = new HashMap<>();

	@PutMapping
	public User updateUser(@Valid @RequestBody User user) {
		int id = user.getId();

		if (allUsers.containsKey(id)) {
			allUsers.put(id, user);
			log.debug("correct update user {}", user);
		} else {
			log.debug("incorrect update user {}", user);
			throw new ValidationException("no user with this id");
		}
		return user;
	}

	@PostMapping
	public User addUser(@Valid @RequestBody User user) {
		user.setId(allUsers.size() + 1);
		allUsers.put(allUsers.size() + 1, user); //new user have id=0?
		log.debug("correct add user {}", user);
		return user;
	}

	@GetMapping("/users")
	public List<User> getAllUsers() {
		log.debug("get all users");
		return new ArrayList<>(allUsers.values());
	}


}
