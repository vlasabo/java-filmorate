package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping(value = "films")
@Slf4j
@ResponseBody
public class FilmController {

	private final FilmStorage filmStorage;
	private final FilmService filmService;
	private final UserService userService;

	@Autowired
	public FilmController(FilmStorage filmStorage, FilmService filmService, UserService userService) {
		this.filmStorage = filmStorage;
		this.filmService = filmService;
		this.userService = userService;
	}

	@PutMapping
	public Film updateFilm(@Valid @RequestBody Film film) {
		return filmStorage.updateFilm(film);
	}

	@PostMapping
	public Film addFilm(@Valid @RequestBody Film film) {
		return filmStorage.addFilm(film);
	}

	@GetMapping
	public List<Film> getAllFilms() {
		log.debug("get all films");
		return filmStorage.getAllFilms();
	}

	@GetMapping("{filmId}")
	public Film getFilmById(@PathVariable Integer filmId) {
		return filmService.getFilmById(filmId);
	}


	@PutMapping("{id}/like/{userId}")
	public Film likeFilm(@PathVariable Integer id, @PathVariable Integer userId) {
		return filmService.like(id, userId, userService, true);
	}

	@DeleteMapping("{id}/like/{userId}")
	public Film unlikeFilm(@PathVariable Integer id, @PathVariable Integer userId) {
		return filmService.like(id, userId, userService, false);
	}

	@GetMapping("popular")
	public List<Film> mostPopularFilms(@RequestParam Optional<String> count) {
		//I know about defaultValue, Optional use for logging
		log.debug("get first {} most popular films", count.orElse("(quantity not specified, so 10)"));
		return filmService.topNFilms(Integer.parseInt(count.orElse("10")));
	}

	@ExceptionHandler
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handle(final MethodArgumentNotValidException e) {
		return new ErrorResponse(
				"Data validation error", e.getMessage()
		);
	}

	@ExceptionHandler
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ErrorResponse handle(final RuntimeException e) {
		return new ErrorResponse(
				"No data found", e.getMessage()
		);
	}

}

