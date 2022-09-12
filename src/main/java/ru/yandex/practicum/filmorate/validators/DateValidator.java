package ru.yandex.practicum.filmorate.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class DateValidator implements ConstraintValidator<ValidFilmDate, LocalDate> {
    static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    public void initialize(ValidFilmDate constraint) {
    }

    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        return !value.isBefore(MIN_RELEASE_DATE);
    }
}