package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "film")
public class FilmController {
	private final ArrayList<Film> allFilms = new ArrayList<>();

	@PutMapping
	public Film updateFilm(@Valid @RequestBody Film film) {
		log.debug("correct update film {}", film);
		log.debug("incorrect update film {}", film);
		return null;
	}

	@PostMapping
	public Film addFilm(@Valid @RequestBody Film film) {

		log.debug("correct add film {}", film);
		log.debug("incorrect add film {}", film);
		return null;
	}

	@GetMapping("/films")
	public List<Film> getAllFilms() {
		log.debug("get all films");
		return null;
	}
}

