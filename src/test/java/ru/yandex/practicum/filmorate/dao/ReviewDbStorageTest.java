package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.film_attributes.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ReviewDbStorageTest {

    private final ReviewStorage storage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    @Test
    public void addShouldBeExceptionIfIncorrectIds(){

        Review review = new Review();
        review.setContent("Good");
        review.setIsPositive(true);
        review.setUserId(100);
        review.setFilmId(100);

        assertThatThrownBy(() -> storage.add(review)).isExactlyInstanceOf(NotFoundException.class);
    }

    @Test
    public void addAndGetShouldBeEquals(){
        Film film = addFilm();
        User user = addUser();

        Review review = new Review();
        review.setContent("Good");
        review.setIsPositive(true);
        review.setFilmId(film.getId());
        review.setUserId(user.getId());

        Review addedReview = storage.add(review);
        assertThat(addedReview)
                .hasFieldOrPropertyWithValue("content", review.getContent())
                .hasFieldOrPropertyWithValue("isPositive", review.getIsPositive())
                .hasFieldOrPropertyWithValue("userId", review.getUserId())
                .hasFieldOrPropertyWithValue("filmId", review.getFilmId())
                .hasFieldOrPropertyWithValue("id", 1);

        Review reviewDB  = storage.get(addedReview.getId());
        assertThat(reviewDB)
                .hasFieldOrPropertyWithValue("content", review.getContent())
                .hasFieldOrPropertyWithValue("isPositive", review.getIsPositive())
                .hasFieldOrPropertyWithValue("userId", review.getUserId())
                .hasFieldOrPropertyWithValue("filmId", review.getFilmId())
                .hasFieldOrPropertyWithValue("id", 1);
    }

    @Test
    public void updateShouldBeEquals(){

        Film film = addFilm();
        User user = addUser();

        Review review = new Review();
        review.setContent("Bad");
        review.setIsPositive(false);
        review.setFilmId(film.getId());
        review.setUserId(user.getId());

        Review addedReview = storage.add(review);

        addedReview.setContent("Bad film");

        Review updatedReview = storage.update(addedReview);
        assertThat(updatedReview).hasFieldOrPropertyWithValue("content", "Bad film");

        Review reviewDB = storage.get(updatedReview.getId());
        assertThat(reviewDB).hasFieldOrPropertyWithValue("content", "Bad film");

    }

    @Test
    public void shouldBeRemoveReview(){
        Film film = addFilm();
        User user = addUser();

        Review review = new Review();
        review.setContent("Bad");
        review.setIsPositive(false);
        review.setFilmId(film.getId());
        review.setUserId(user.getId());

        int id = storage.add(review).getId();

        assertThat(storage.get(id)).isNotNull();

        storage.remove(id);

        assertThatThrownBy(() -> storage.get(id)).isExactlyInstanceOf(NotFoundException.class);
    }

    @Test
    public void shouldBeReturnListCount100(){
        Film film = addFilm();
        User user = addUser();

        Review review = new Review();
        review.setContent("Bad movie");
        review.setIsPositive(false);
        review.setFilmId(film.getId());
        review.setUserId(user.getId());

        int id = storage.add(review).getId();

        List<Review> reviews = storage.getReviews(100);
        Optional<Review> findReview = reviews.stream().filter(r -> r.getId() == id)
                .findFirst();
        assertThat(findReview).isPresent().get().hasFieldOrPropertyWithValue("content", "Bad movie")
                .hasFieldOrPropertyWithValue("isPositive", false);
    }

    @Test
    public void shouldReturnListCount1(){
        Film film = addFilm();
        User user = addUser();
        User user2 = addUser();

        Review review = new Review();
        review.setContent("Bad movie");
        review.setIsPositive(false);
        review.setFilmId(film.getId());
        review.setUserId(user.getId());

        Review review1 = new Review();
        review1.setContent("Good movie");
        review1.setIsPositive(true);
        review1.setFilmId(film.getId());
        review1.setUserId(user2.getId());

        storage.add(review);
        storage.add(review1);

        List<Review> reviews = storage.getReviews(1);
        assertThat(reviews).isNotNull().asList().hasSize(1);
    }

    @Test
    public void shouldBeReturnEmptyList(){

        Film film = addFilm();
        User user = addUser();

        Review review = new Review();
        review.setContent("Bad movie");
        review.setIsPositive(false);
        review.setFilmId(film.getId());
        review.setUserId(user.getId());

        storage.add(review);

        List<Review> reviewList = storage.getReviewsByFilm(10, 10);
        assertThat(reviewList).isNotNull().asList().hasSize(0);

    }

    @Test
    public void shouldBeReturnListSize1(){
        Film film = addFilm();
        User user = addUser();

        Review review = new Review();
        review.setContent("Bad movie");
        review.setIsPositive(false);
        review.setFilmId(film.getId());
        review.setUserId(user.getId());

        storage.add(review);

        List<Review> reviewList = storage.getReviewsByFilm(film.getId(), 2);
        assertThat(reviewList).isNotNull().asList().hasSize(1);
    }

    @Test
    public void shouldBeUseful1After2After0(){

        Review review = addReview();

        User user1 = addUser();
        User user2 = addUser();
        User user3 = addUser();

        storage.like(review.getId(), user1.getId());
        storage.like(review.getId(), user2.getId());
        storage.dislike(review.getId(), user3.getId());

        assertThat(storage.get(review.getId()))
                .isNotNull().hasFieldOrPropertyWithValue("useful", 1);

        storage.deleteDislike(review.getId(), user3.getId());
        storage.deleteDislike(review.getId(), 10);

        assertThat(storage.get(review.getId()))
                .isNotNull().hasFieldOrPropertyWithValue("useful", 2);

        storage.deleteLike(review.getId(), user1.getId());
        storage.deleteLike(review.getId(), user2.getId());

        assertThat(storage.get(review.getId()))
                .isNotNull().hasFieldOrPropertyWithValue("useful", 0);

    }

    private Review addReview(){
        Film film = addFilm();
        User user = addUser();

        Review review = new Review();
        review.setContent("Bad movie");
        review.setIsPositive(false);
        review.setFilmId(film.getId());
        review.setUserId(user.getId());

        return storage.add(review);
    }

    private User addUser(){
        User user = new User("email@yandex.ru", "login1", "name", LocalDate.now());
        return userStorage.addUser(user);
    }

    private Film addFilm(){
        Film film = new Film("film1", "film1", LocalDate.of(2000, 1,1), 120);
        film.setMpa(new Mpa("G", 1));

        return filmStorage.addFilm(film);
    }
}
