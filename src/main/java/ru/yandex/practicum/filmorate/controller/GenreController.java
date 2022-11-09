package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.film_attributes.Genre;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping(value = "genres")
@Slf4j
public class GenreController {
    private final FilmService filmService;

    @GetMapping
    public List<Genre> getAllGenres() {
        log.debug("get all genres");
        return filmService.getAllGenres();
    }

    @GetMapping("/{id}")
    public Genre getGenreById(@PathVariable Integer id) {
        log.debug("get genre by id {}", id);
        return filmService.getGenreById(id);
    }
}
