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

		return null;
	}

	@PostMapping
	public User addUser(@RequestBody User user) {

		return null;
	}

	@GetMapping("/users")
	public List<User> getAllUsers() {

		return null;
	}


}
