package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDate;

@Data
public class User {

	private int id;

	@Email(message = "{validation.email.Email}")
	@NotEmpty
	private final String email;
	@NotEmpty
	private final String login;
	private final String name;
	@Past
	private final LocalDate birthday;
}
