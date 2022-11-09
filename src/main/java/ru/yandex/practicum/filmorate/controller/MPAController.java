package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.film_attributes.Mpa;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping(value = "mpa")
@Slf4j
public class MPAController {
    private final FilmService filmService;

    @GetMapping
    public List<Mpa> getAllGenres() {
        log.debug("get all genres");
        return filmService.getAllMpa();
    }

    @GetMapping("/{id}")
    public Mpa getMpaById(@PathVariable Integer id) {
        log.debug("get genre by id {}", id);
        return filmService.getMpaById(id);
    }
}
