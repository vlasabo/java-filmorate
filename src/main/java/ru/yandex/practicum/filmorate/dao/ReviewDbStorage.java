package ru.yandex.practicum.filmorate.dao;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;

import java.util.List;

@Component
public class ReviewDbStorage implements ReviewStorage {

    @Override
    public Review add(Review review) {
        return null;
    }

    @Override
    public Review update(Review review) {
        return null;
    }

    @Override
    public Review get(int id) {
        return null;
    }

    @Override
    public void like(int id, int userId) {

    }

    @Override
    public void dislike(int id, int userId) {

    }

    @Override
    public void deleteLike(int id, int userId) {

    }

    @Override
    public void deleteDislike(int id, int userId) {

    }

    @Override
    public List<Review> getReviews(int count) {
        return null;
    }

    @Override
    public List<Review> getReviewsByFilm(int filmId, int count) {
        return null;
    }

}
