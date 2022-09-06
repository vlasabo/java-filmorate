package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "film")
public class FilmController {

	@PutMapping
	public Film updateFilm(@RequestBody Film film) {

		return null;
	}

	@PostMapping
	public Film addFilm(@RequestBody Film film) {

		return null;
	}

	@GetMapping("/films")
	public List<Film> getAllFilms() {

		return null;
	}
}

//TODO: check in tests endpoints for Film and User controllers