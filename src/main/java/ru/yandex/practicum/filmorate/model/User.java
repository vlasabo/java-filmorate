package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.util.Date;

@Data
public class User {

	private int id;
	private String email;
	private String login;
	private final String name;
	private final Date birthday;
}
