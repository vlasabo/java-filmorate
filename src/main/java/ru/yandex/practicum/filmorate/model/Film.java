package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import org.springframework.cglib.core.Local;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;
import java.time.LocalDate;


@Data
public class Film {

	private int id;
	@NotEmpty
	private final String name;
	private String description;
	private final LocalDate releaseDate;
	@Positive
	private final int duration;
	private static final LocalDate FIRST_FILM_RELEASE_DAY = LocalDate.of(1895, 12, 28);
}
