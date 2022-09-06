package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import javax.validation.constraints.*;
import java.util.Date;

@Data
public class User {

	private int id;

	@Email
	@NotEmpty
	private String email;
	@NotEmpty
	private String login;
	private final String name;
	@Past
	private final Date birthday;
}
