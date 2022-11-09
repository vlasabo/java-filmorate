package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewStorage storage;
    private final EventService eventService;

    public List<Review> getReviews(int filmId, int count) {
        return filmId == -1 ? storage.getReviews(count) : storage.getReviewsByFilm(filmId, count);
    }

    public Review addReview(Review review) {
        Review added = storage.add(review);
        eventService.addAddedReviewEvent(added.getUserId(), added.getId());
        return added;
    }

    public Review updateReview(Review review) {
        Review updated = storage.update(review);
        eventService.addUpdatedReviewEvent(updated.getUserId(), updated.getId());
        return updated;
    }

    public Review getReview(int id) {
        return storage.get(id);
    }

    public void removeReview(int id) {
        Review review = getReview(id);
        storage.remove(id);
        eventService.addRemovedReviewEvent(review.getUserId(), review.getId());
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
