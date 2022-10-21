package ru.yandex.practicum.filmorate.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.ValidationException;


@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundException extends ValidationException {
	public NotFoundException(String arg) {
		super(arg);
	}
}
