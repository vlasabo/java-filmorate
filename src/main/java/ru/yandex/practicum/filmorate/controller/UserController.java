package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "user")
public class UserController {

	@PutMapping
	public User updateUser(@RequestBody User user) {

		log.trace("correct update user {}", user);
		log.trace("incorrect update user {}", user);
		return null;
	}

	@PostMapping
	public User addUser(@RequestBody User user) {
		log.trace("correct add user {}", user);
		log.trace("incorrect add user {}", user);
		return null;
	}

	@GetMapping("/users")
	public List<User> getAllUsers() {
		log.trace("get all users");
		return null;
	}


}
