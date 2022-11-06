package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewStorage {

    Review add(Review review);
    Review update(Review review);
    Review get(int id);
    void remove(int id);

    void like(int id, int userId);
    void dislike(int id, int userId);
    void deleteLike(int id, int userId);
    void deleteDislike(int id, int userId);

    List<Review> getReviews(int count);
    List<Review> getReviewsByFilm(int filmId, int count);

}
