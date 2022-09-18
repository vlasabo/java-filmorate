package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public Film like(int filmId, int userId, UserService userService, boolean like) {
        Film film = getFilmById(filmId);
        userService.getUserById(userId); //check user exist
        if (like) {
            film.addLike(userId);
            log.debug("add like to film with id={} from user with id={}", filmId, userId);
        } else {
            film.removeLike(userId);
            log.debug("remove like to film with id={} from user with id={}", filmId, userId);
        }
        return film;
    }

    public List<Film> topNFilms(int quantity) {
        if (quantity <= 0) {
            quantity = 10;
        }

        List<Film> allFilms = filmStorage.getAllFilms();
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
}
