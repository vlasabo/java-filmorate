package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
	private final HashMap<Integer, Film> allFilms = new HashMap<>();

	@Override
	public Film addFilm(Film film) {
		film.setId(allFilms.size() + 1);
		allFilms.put(allFilms.size() + 1, film);
		log.debug("correct adding film {}", film);
		return film;
	}

	@Override
	public List<Film> getAllFilms() {
		return new ArrayList<>(allFilms.values());
	}

	@Override
	public Film updateFilm(Film film) {
		int id = film.getId();

		if (allFilms.containsKey(id)) {
			allFilms.put(id, film);
			log.debug("correct update film {}", film);
		} else {
			log.debug("incorrect update film {}", film);
			throw new NotFoundException(String.format("Film by id %d not found", id));
		}
		return film;
	}
}
