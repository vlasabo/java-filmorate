package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.*;
import java.time.LocalDate;

@Data
public class User {

    private int id;

    @Email(message = "EMAIL IS INCORRECT")
    @NotEmpty
    private final String email;
    @NotEmpty(message = "LOGIN IS EMPTY")
    private final String login;
    private String name;
    @Past(message = "DATE IS INCORRECT")
    private final LocalDate birthday;


}
