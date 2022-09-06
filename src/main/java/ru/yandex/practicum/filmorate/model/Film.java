package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.cglib.core.Local;

import javax.validation.constraints.FutureOrPresent;
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
    private final LocalDate releaseDate;
    @Positive(message = "DURATION IS NEGATIVE OR ZERO")
    private final int duration;
    public static final LocalDate FIRST_FILM_RELEASE_DAY = LocalDate.of(1895, 12, 28);
}
