package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
public class ReviewController {

    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/reviews")
    public List<Review> getReviews(@RequestParam Optional<Integer> filmId,
                                   @RequestParam(defaultValue = "10") int count){
        return reviewService.getReviews(filmId, count);
    }

    @PostMapping("/reviews")
    public Review postReview(@Valid @RequestBody Review review){
        return reviewService.addReview(review);
    }

    @PutMapping("/reviews")
    public Review putReview(@Valid @RequestBody Review review){
        return reviewService.updateReview(review);
    }

    @GetMapping("/reviews/{id}")
    public Review getReview(@PathVariable int id){
        return reviewService.getReview(id);
    }

    @DeleteMapping("reviews/{id}")
    public void removeReview(@PathVariable int id){
        reviewService.removeReview(id);
    }

    @PutMapping("/reviews/{id}/like/{userId}")
    public void putLike(@PathVariable int id,
                        @PathVariable int userId){
        reviewService.addLike(id, userId);
    }

    @PutMapping("/reviews/{id}/dislike/{userId}")
    public void putDislike(@PathVariable int id,
                           @PathVariable int userId){
        reviewService.addDislike(id, userId);
    }

    @DeleteMapping("/reviews/{id}/like/{userId}")
    public void deleteLike(@PathVariable int id,
                           @PathVariable int userId){
        reviewService.deleteLike(id, userId);
    }

    @DeleteMapping("/reviews/{id}/dislike/{userId}")
    public void deleteDislike(@PathVariable int id,
                              @PathVariable int userId){
        reviewService.deleteDislike(id, userId);
    }
}
