package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectorService {

    private final DirectorStorage storage;

    public List<Director> getDirectors() {
        return storage.getAll();
    }

    public Director getDirectorById(int id) {
        return storage.get(id);
    }

    public Director addDirector(Director director) {
        return storage.add(director);
    }

    public Director updateDirector(Director director) {
        return storage.update(director);
    }

    public void removeDirector(int id) {
        storage.delete(id);
    }
}
