package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;

import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {

    private final ReviewStorage storage;

    @Autowired
    public ReviewService(ReviewStorage storage){
        this.storage = storage;
    }

    public List<Review> getReviews(Optional<Integer> filmId, int count) {
        if (filmId.isEmpty()) {
            return storage.getReviews(count);
        }
        return storage.getReviewsByFilm(filmId.get(), count);
    }

    public Review addReview(Review review) {
        return storage.add(review);
    }

    public Review updateReview(Review review) {
        return storage.update(review);
    }

    public Review getReview(int id) {
        return storage.get(id);
    }

    public void addLike(int id, int userId) {
        storage.like(id, userId);
    }

    public void addDislike(int id, int userId) {
        storage.dislike(id, userId);
    }

    public void deleteLike(int id, int userId) {
        storage.deleteLike(id, userId);
    }

    public void deleteDislike(int id, int userId) {
        storage.deleteDislike(id, userId);
    }
}
