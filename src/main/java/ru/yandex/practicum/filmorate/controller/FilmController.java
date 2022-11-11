package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "films")
@Slf4j
public class FilmController {

    private final FilmService filmService;
    private final UserService userService;

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        return filmService.updateFilm(film);
    }

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        return filmService.addFilm(film);
    }

    @GetMapping
    public List<Film> getAllFilms() {
        log.debug("get all films");
        return filmService.getAllFilms();
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
    public List<Film> mostPopularFilms( @RequestParam(required = false, defaultValue = "10") int count,
                                        @RequestParam(required = false) Integer genreId,
                                        @RequestParam(required = false) String year) {
        return filmService.getPopularFilms(count, genreId, year);
    }

    @GetMapping("/common")
    public List<Film> mostPopularFilmsIntersectionWithFriend(@RequestParam Integer userId, @RequestParam Integer friendId) {
        log.debug("get movies that friends watched, sorted by popularity by userId {} and userId {}", userId, friendId);
        if (!userService.getUserById(userId).getFriends().containsKey(friendId)) {
            log.debug("users with userId {} and userId {} are not friends", userId, friendId);
        }

        return filmService.getMostPopularFilmsIntersectionWithFriend(userId, friendId);
    }
    
    @DeleteMapping("/{filmId}")
	public void deleteFilm(@PathVariable int filmId){
		filmService.deleteFilm(filmId);
	}


	@GetMapping("search")
	public List<Film> searchFilmsByString(@RequestParam String query, @RequestParam String by) {
		return filmService.searchFilmsByString(query, by);
	}


    @GetMapping("/director/{directorId}")
    public List<Film> getFilmsByDirector(@PathVariable int directorId,
                                   @RequestParam String sortBy){
        return filmService.getFilmByDirector(directorId, sortBy);
    }
}

