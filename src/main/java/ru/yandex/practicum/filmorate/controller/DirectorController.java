package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.service.DirectorService;
import ru.yandex.practicum.filmorate.model.Director;

import javax.validation.Valid;
import java.util.List;

@RestController
public class DirectorController {
    private final DirectorService service;

    @Autowired
    public DirectorController(DirectorService service) {
        this.service = service;
    }

    @GetMapping("/directors")
    public List<Director> getDirectors(){
        return service.getDirectors();
    }

    @GetMapping("/directors/{id}")
    public Director getDirectorById(@PathVariable int id){
        return service.getDirectorById(id);
    }

    @PostMapping("/directors")
    public Director addDirector(@Valid @RequestBody Director director){
        return service.addDirector(director);
    }

    @PutMapping("/directors")
    public Director updateDirector(@Valid @RequestBody Director director){
        return service.updateDirector(director);
    }

    @DeleteMapping("/directors/{id}")
    public void removeDirector(@PathVariable int id){
        service.removeDirector(id);
    }
}
