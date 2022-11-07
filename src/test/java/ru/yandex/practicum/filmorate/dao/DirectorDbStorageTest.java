package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class DirectorDbStorageTest {

    private final DirectorStorage storage;

    @Test
    public void addTestShouldBeAdded(){
        assertThat(storage.getAll()).isNotNull()
                .asList().hasSize(0);

        newDirector();
        assertThat(storage.getAll()).isNotNull()
                .asList().hasSize(1);
        assertThat(storage.get(1))
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", 1);

    }

    @Test
    public void updateTestShouldBeUpdated(){
        newDirector();
        Director directorDb = storage.get(1);
        assertThat(directorDb).isNotNull()
                .hasFieldOrPropertyWithValue("name", "dir");

        directorDb.setName("dir updated");
        storage.update(directorDb);

        directorDb = storage.get(1);
        assertThat(directorDb).isNotNull()
                .hasFieldOrPropertyWithValue("name", "dir updated");


    }

    @Test
    public void deleteTestShouldBeDeleted(){
        newDirector();
        assertThat(storage.getAll()).isNotNull()
                .asList().hasSize(1);
        storage.delete(1);
        assertThat(storage.getAll()).isNotNull()
                .asList().hasSize(0);
    }

    private void newDirector(){
        Director director = new Director();
        director.setName("dir");
        storage.add(director);
    }

}
