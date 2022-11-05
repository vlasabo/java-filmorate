package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.dao.FilmDbStorage;
import ru.yandex.practicum.filmorate.dao.UserDbStorage;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.film_attributes.Mpa;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class SearchFilmsByStringTests {
    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;


    @BeforeEach
    public void PrepareDBForTests() {

        List<User> users = new ArrayList<>();

        users.add(new User("new1@email.com", "login1", "name1"
                        ,  LocalDate.of(1999, 12, 1)));
        users.add(new User("new2@email.com", "login2", "name2"
                ,  LocalDate.of(1999, 12, 1)));
        users.add(new User("new3@email.com", "login3", "name3"
                ,  LocalDate.of(1999, 12, 1)));
        users.add(new User("new4@email.com", "login4", "name4"
                ,  LocalDate.of(1999, 12, 1)));
        users.add(new User("new5@email.com", "login5", "name5"
                ,  LocalDate.of(1999, 12, 1)));
        users.add(new User("new6@email.com", "login6", "name6"
                ,  LocalDate.of(1999, 12, 1)));
        users.add(new User("new7@email.com", "login7", "name7"
                ,  LocalDate.of(1999, 12, 1)));
        users.add(new User("new8@email.com", "login8", "name8"
                ,  LocalDate.of(1999, 12, 1)));


        for (User user : users) {
            userStorage.addUser(user);
        }

        List<Film> films = new ArrayList<>();

        films.add(new Film("Терминатор", "Описание1"
                , LocalDate.of(1999, 12, 1), 120));
        films.add(new Film("Судный день терминатор", "Описание2"
                , LocalDate.of(1999, 12, 1), 120));
        films.add(new Film("Генезис терминатор восстание", "Описание3"
                , LocalDate.of(1999, 12, 1), 120));
        films.add(new Film("Крепкий орешек", "Описание4"
                , LocalDate.of(1999, 12, 1), 120));
        films.add(new Film("Остров сокровищ", "Описание5"
                , LocalDate.of(1999, 12, 1), 120));
        films.add(new Film("Сокровища острова", "Описание6"
                , LocalDate.of(1999, 12, 1), 120));
        films.add(new Film("На островах сокровищ", "Описание7"
                , LocalDate.of(1999, 12, 1), 120));


        for (Film film : films) {
            film.setMpa(new Mpa("G",1 ));
            filmStorage.addFilm(film);
        }

        filmStorage.addLike(3,1);
        filmStorage.addLike(3,2);
        filmStorage.addLike(3,3);
        filmStorage.addLike(3,4);

        filmStorage.addLike(1,2);
        filmStorage.addLike(1,3);

        filmStorage.addLike(2,5);
        filmStorage.addLike(2,6);
        filmStorage.addLike(2,7);

        filmStorage.addLike(4,5);
        filmStorage.addLike(5,6);
        filmStorage.addLike(6,7);
        filmStorage.addLike(7,7);
        filmStorage.addLike(7,1);

    }

    @Test
    public void testFindFilmByString() {

        List<Film> testFilms = filmStorage.searchNFilmsByString("терминатор", 3);
        assertThat(testFilms).hasSize(3);
        assertThat(testFilms.get(0)).hasFieldOrPropertyWithValue("id", 3);
        assertThat(testFilms.get(1)).hasFieldOrPropertyWithValue("id", 2);
        assertThat(testFilms.get(2)).hasFieldOrPropertyWithValue("id", 1);
    }

    @Test
    public void testThatOrderedByLikes() {
        List<Film> testFilms = filmStorage.searchNFilmsByString("остров", 5);
        assertThat(testFilms).hasSize(3);
        assertThat(testFilms.get(0)).hasFieldOrPropertyWithValue("id", 7);
        assertThat(testFilms.get(1)).hasFieldOrPropertyWithValue("id", 5);
        assertThat(testFilms.get(2)).hasFieldOrPropertyWithValue("id", 6);

        assertThat(testFilms.get(0).getLikes().size() >= testFilms.get(1).getLikes().size());
        assertThat(testFilms.get(1).getLikes().size() >= testFilms.get(2).getLikes().size());
    }

    @Test
    public void testFilmNumberOutput() {
        List<Film> testFilms = filmStorage.searchNFilmsByString("о", 5);
        assertThat(testFilms).hasSize(5);
    }


}