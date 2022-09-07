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
    private String email;
    @NotEmpty(message = "LOGIN IS EMPTY")
    private String login;
    private String name;
    @Past(message = "DATE IS INCORRECT")
    private LocalDate birthday;

    public User(String email, String login, String name, LocalDate birthday) {
        this.email = email;
        this.login = login;
        this.name = name;
        this.birthday = birthday;
    }

    public User() {

    }


}
