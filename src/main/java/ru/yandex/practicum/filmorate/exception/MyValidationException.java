package ru.yandex.practicum.filmorate.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.ValidationException;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class MyValidationException extends ValidationException {
	public MyValidationException(String arg) {
		super(arg);
	}
}
