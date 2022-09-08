package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;
import java.time.LocalDate;


@Data
public class Film {

    private int id;


    @NotEmpty(message = "NAME IS EMPTY")
    private final String name;
    @Length(max = 200, message = "LENGTH OF DESCRIPTION IS OVER 200")
    private String description;
    private LocalDate releaseDate;
    @Positive(message = "DURATION IS NEGATIVE OR ZERO")
    private final int duration;
    public static final LocalDate FIRST_FILM_RELEASE_DAY = LocalDate.of(1895, 12, 28);

    public Film(String title, String description, LocalDate releaseDate, int duration) {
        this.name = title;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
    }


}
