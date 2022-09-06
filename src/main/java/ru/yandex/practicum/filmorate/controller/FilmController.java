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
		log.trace("correct update film {}", film);
		log.trace("incorrect update film {}", film);
		return null;
	}

	@PostMapping
	public Film addFilm(@RequestBody Film film) {

		log.trace("correct add film {}", film);
		log.trace("incorrect add film {}", film);
		return null;
	}

	@GetMapping("/films")
	public List<Film> getAllFilms() {
		log.trace("get all films");
		return null;
	}
}

//TODO: check in tests endpoints for Film and User controllers