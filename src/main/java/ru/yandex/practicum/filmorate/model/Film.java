package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import org.hibernate.validator.constraints.Length;
import ru.yandex.practicum.filmorate.validators.ValidFilmDate;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;


@Data
public class Film {

    private int id;
    @NotEmpty(message = "NAME IS EMPTY")
    private final String name;
    @Length(max = 200, message = "LENGTH OF DESCRIPTION IS OVER 200")
    private String description;
    @ValidFilmDate(message = "INCORRECT RELEASE DATE")
    private LocalDate releaseDate;
    @Positive(message = "DURATION IS NEGATIVE OR ZERO")
    private final int duration;
    private Set<Integer> likes = new HashSet<>();

    public Film(String title, String description, LocalDate releaseDate, int duration) {
        this.name = title;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
    }

    public void addLike(int userId){
        likes.add(userId);
    }

    public void removeLike(int userId){
        likes.remove(userId);
    }

    public int howManyLikes(){
        return likes.size();
    }
}
