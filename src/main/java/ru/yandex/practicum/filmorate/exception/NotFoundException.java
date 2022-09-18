package ru.yandex.practicum.filmorate.exception;

import javax.validation.ValidationException;


public class NotFoundException extends ValidationException {
	public NotFoundException(String arg) {
		super(arg);
	}
}
