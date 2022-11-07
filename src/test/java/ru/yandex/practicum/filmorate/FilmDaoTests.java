package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.film_attributes.Genre;
import ru.yandex.practicum.filmorate.model.film_attributes.Mpa;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class FilmDaoTests {


    @Autowired
    private FilmStorage filmStorage;
    @Autowired
    private UserStorage userStorage;

    @Autowired
    private FilmService filmService;

    @Autowired
    private DirectorStorage directorStorage;

    @Test
    public void testFindFilmById() {
        addFilm(1);
        Optional<Film> filmOptional = filmStorage.getFilmById(1);
        assertThat(filmOptional).isPresent().hasValueSatisfying(filmFromStorage
                -> assertThat(filmFromStorage).hasFieldOrPropertyWithValue("description", "testDescr1"));
    }

    @Test
    public void testGtMostPopularFilmsIntersectionWithFriend() {
        Film film1 = addFilm(1);
        Film film2 = addFilm(2);
        Film film3 = addFilm(3);
        Film film4 = addFilm(4);
        Film film5 = addFilm(5);

        addUser(1);
        addUser(2);
        addUser(3);

        film1 = setLikeToFilm(film1, 1, 1);
        film2 = setLikeToFilm(film2, 2, 1);
        film3 = setLikeToFilm(film3, 3, 1);

        film2 = setLikeToFilm(film2, 2, 2);
        film3 = setLikeToFilm(film3, 3, 2);
        film4 = setLikeToFilm(film4, 4, 2);

        film3 = setLikeToFilm(film3, 3, 3);


        var resultListFilms = new ArrayList<Film>();
        resultListFilms.add(film3);
        resultListFilms.add(film2);
        Assertions.assertEquals(filmService.getMostPopularFilmsIntersectionWithFriend(1, 2), resultListFilms);
    }

    @Test
    public void getFilmByDirectorShouldBeSorted(){

        User user1 = addUser(1);
        User user2 = addUser(2);

        Film film1 = addFilm(1);
        Film film2 = addFilm(2);
        Film film3 = addFilm(3);

        Director director1 = addDirector(1);
        Director director2 = addDirector(2);
        Director director3 = addDirector(3);

        film1.setDirectors(Set.of(director1, director3));
        film1 = filmStorage.updateFilm(film1);

        film2.setDirectors(Set.of(director1, director2, director3));
        filmStorage.updateFilm(film2);

        film3.setDirectors(Set.of(director2, director3));
        filmStorage.updateFilm(film3);

        filmStorage.addLike(film1.getId(), user1.getId());
        filmStorage.addLike(film1.getId(), user2.getId());

        List<Film> sortedFilmByDirector = filmStorage.getFilmByDirector(director1.getId(), "likes");
        assertThat(sortedFilmByDirector)
                .isNotNull()
                .asList()
                .hasSize(2);

        assertThat(sortedFilmByDirector.get(0)).isNotNull()
                .hasFieldOrPropertyWithValue("id", 2);
        assertThat(sortedFilmByDirector.get(1)).isNotNull()
                .hasFieldOrPropertyWithValue("id", 1);

        sortedFilmByDirector = filmStorage.getFilmByDirector(director1.getId(), "year");
        assertThat(sortedFilmByDirector.get(0)).isNotNull()
                .hasFieldOrPropertyWithValue("id", 1);
        assertThat(sortedFilmByDirector.get(1)).isNotNull()
                .hasFieldOrPropertyWithValue("id", 2);

        sortedFilmByDirector = filmStorage.getFilmByDirector(director3.getId(), "year");
        assertThat(sortedFilmByDirector)
                .isNotNull()
                .asList()
                .hasSize(3);

        assertThat(sortedFilmByDirector.get(0)).isNotNull()
                .hasFieldOrPropertyWithValue("id", 1);
        assertThat(sortedFilmByDirector.get(2)).isNotNull()
                .hasFieldOrPropertyWithValue("id", 3);


    }

    private Director addDirector(int i){
        Director director = new Director();
        director.setName("director" + i);
        directorStorage.add(director);
        return director;
    }

    private Film addFilm(int i) {
        Film film = new Film("testTitle" + i, "testDescr" + i, LocalDate.ofEpochDay(1), i * 100);
        film.setMpa(new Mpa("testMpa" + i, i));
        var genres = new ArrayList<Genre>();
        genres.add(new Genre("testGenre", 1));
        film.setGenres(genres);
        filmStorage.addFilm(film);
        return film;
    }

    private User addUser(int i) {
        User user = new User("test@test" + i + ".com", "login" + i, "vladimir" + i, LocalDate.ofEpochDay(1));
        userStorage.addUser(user);
        return user;
    }

    private Film setLikeToFilm(Film film, int filmId, int userId) {
        filmStorage.addLike(filmId, userId);
        film.getLikes().add(userId);
        return film;
    }

    @Test
    public void recommendationsTest() {
        addUser(1);
        addUser(2);
        Film film1 = addFilm(1);
        Film film2 = addFilm(2);
        Film film3 = addFilm(3);
        Film film4 = addFilm(4);
        // лайков ещё нет, количество рекомендаций будет = 0
        Assertions.assertEquals(0, userStorage.getRecommendations(1).size());
        setLikeToFilm(film1, 1, 1);
        setLikeToFilm(film1, 1, 2);
        // все лайки общие, количество рекомендаций будет = 0
        Assertions.assertEquals(0, userStorage.getRecommendations(1).size());
        setLikeToFilm(film2, 2, 1);
        setLikeToFilm(film2, 2, 2);
        setLikeToFilm(film3, 3, 1);
        // есть совпадающие лайки на 1 и 2 фильме, в рекомендациях будет только фильм 3
        Assertions.assertEquals(1, userStorage.getRecommendations(2).size());
        Assertions.assertEquals(3, userStorage.getRecommendations(2).get(0).getId());
        setLikeToFilm(film4, 4, 2);
        // есть совпадающие лайки на 1 и 2 фильме, в рекомендациях будет только фильм 4
        Assertions.assertEquals(1, userStorage.getRecommendations(1).size());
        Assertions.assertEquals(4, userStorage.getRecommendations(1).get(0).getId());
        // должно вернуть 404 для несуществующего пользователя
        Assertions.assertThrows(NotFoundException.class, () -> userStorage.getRecommendations(42));
    }
    @Test
    public void deleteFilmTest() {
        addFilm(1);
        Assertions.assertEquals("testTitle1", filmStorage.getFilmById(1).get().getName());
        filmStorage.deleteFilm(1);
        Assertions.assertEquals(0, filmStorage.getAllFilms().size());
    }

    @Test
    public void getPopularFilms() {
        addFilm(1);
        addFilm(2);
        addFilm(3);
        addUser(1);
        filmStorage.addLike(1, 1);
        filmStorage.addLike(2, 1);
        Assertions.assertEquals(0, filmStorage.getPopularFilms(10,"2000").size());
        Assertions.assertEquals(3, filmStorage.getPopularFilms(10,1,"1970").size());
        Assertions.assertEquals(0, filmStorage.getPopularFilms(0,3).size());
    }

}