package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.util.Date;

@Data
public class Film {

	private int id;
	private final String name;
	private String description;
	private final Date releaseDate;
	private final int duration;
}
