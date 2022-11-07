package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.service.DirectorService;
import ru.yandex.practicum.filmorate.model.Director;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/directors")
public class DirectorController {
    private final DirectorService service;

    @Autowired
    public DirectorController(DirectorService service) {
        this.service = service;
    }

    @GetMapping
    public List<Director> getDirectors(){
        return service.getDirectors();
    }

    @GetMapping("/{id}")
    public Director getDirectorById(@PathVariable int id){
        return service.getDirectorById(id);
    }

    @PostMapping
    public Director addDirector(@Valid @RequestBody Director director){
        return service.addDirector(director);
    }

    @PutMapping
    public Director updateDirector(@Valid @RequestBody Director director){
        return service.updateDirector(director);
    }

    @DeleteMapping("/{id}")
    public void removeDirector(@PathVariable int id){
        service.removeDirector(id);
    }
}
