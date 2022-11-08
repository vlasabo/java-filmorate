package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.film_attributes.Genre;
import ru.yandex.practicum.filmorate.model.film_attributes.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
	private final EventService eventService;

    @Autowired
    public FilmService(FilmStorage filmDbStorage, EventService eventService) {
        this.filmStorage = filmDbStorage;
		this.eventService = eventService;
	}


	public Film like(int filmId, int userId, UserService userService, boolean like) {
		Film film = getFilmById(filmId);
		userService.getUserById(userId); //check user exist
		if (like) {
			film.addLike(userId);
			filmStorage.addLike(filmId, userId);
			log.debug("add like to film with id={} from user with id={}", filmId, userId);
			eventService.addAddedLikeEvent(userId, filmId);
		} else {
			film.removeLike(userId);
			filmStorage.removeLike(filmId, userId);
			log.debug("remove like to film with id={} from user with id={}", filmId, userId);
			eventService.addRemovedLikeEvent(userId, filmId);
		}
		return film;
	}

	public List<Film> topNFilms(int quantity) {
		if (quantity <= 0) {
			quantity = 10;
		}

		List<Film> allFilms = getAllFilms();
		return allFilms.stream().sorted(Comparator.comparing(Film::howManyLikes).reversed())
				.limit(quantity).collect(Collectors.toList());
	}

	public Film getFilmById(int filmId) {
		Optional<Film> filmOptional = filmStorage.getAllFilms().stream().filter(f -> f.getId() == filmId).findFirst();
		if (filmOptional.isPresent()) {
			log.debug("get film by id {}", filmId);
			return filmOptional.get();
		} else {
			log.debug("Film by id {} not found", filmId);
			throw new NotFoundException(String.format("Film by id %d not found", filmId));
		}
	}

	public List<Film> getAllFilms() {
		return filmStorage.getAllFilms();
	}

	public Film addFilm(Film film) {
		return filmStorage.addFilm(film);
	}

	public Film updateFilm(Film film) {
		return filmStorage.updateFilm(film);
	}

	public List<Genre> getAllGenres() {
		return filmStorage.getAllGenres();
	}

	public Genre getGenreById(Integer genreId) {
		return filmStorage.getGenreById(genreId);
	}

	public List<Mpa> getAllMpa() {
		return filmStorage.getAllMpa();
	}

	public Mpa getMpaById(Integer mpaId) {
		return filmStorage.getMpaById(mpaId);
	}

	public List<Film> searchFilmsByString(String query, String by) {

		if (by == null) {
			throw new RuntimeException("searchFilmsByString: 'by' is null");
		}

		List<String> searchByArr  = new ArrayList<>();
		if (by.contains("title")) searchByArr.add("title");
		if (by.contains("director")) searchByArr.add("director");

		String searchBy;
		switch (searchByArr.size()) {
			case 1:
				searchBy = searchByArr.get(0);
				break;
			case 2:
				searchBy = "both";
				break;
			default:
				throw new RuntimeException("searchFilmsByString: 'by' has invalid value:" + by);
		}

		return filmStorage.searchFilmsByString(query, searchBy);
	}
	public void deleteFilm(int id){
		getFilmById(id);
		filmStorage.deleteFilm(id);
		log.debug("Delete  film {}", id);
	}

    public List<Film> getMostPopularFilmsIntersectionWithFriend(int userId, int friendId) {
        List<Film> allFilmsUserLiked = filmStorage.getAllFilmsUserLiked(userId);
        List<Film> allFilmsFriendLiked = filmStorage.getAllFilmsUserLiked(friendId);

        return allFilmsUserLiked.stream()
                .filter(allFilmsFriendLiked::contains)
                .sorted((o1, o2) -> o2.getLikes().size() - o1.getLikes().size())
                .collect(Collectors.toList());
    }

    public List<Film> getFilmByDirector(int directorId, Optional<String> sortBy) {
		List<String> validSort = List.of("likes", "year");

		if (sortBy.isEmpty() || !validSort.contains(sortBy.get())){
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid sortBy");
		}

		return filmStorage.getFilmByDirector(directorId, sortBy.get());
    }

	public List<Film> getPopularFilms(int count, Optional<Integer> genreId, Optional<String> year) {
		List<Film> popularFilms;
		if (genreId.isPresent() && year.isPresent()) {
			popularFilms = filmStorage.getPopularFilms(count, genreId.get(), year.get());
		} else if (genreId.isPresent()) {
			popularFilms = filmStorage.getPopularFilms(count, genreId.get());
		} else if (year.isPresent()) {
			popularFilms = filmStorage.getPopularFilms(count, year.get());
		} else {
			popularFilms = topNFilms(count);
		}
		return popularFilms;
	}

}
