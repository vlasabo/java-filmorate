package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import org.springframework.data.relational.core.mapping.Column;
import ru.yandex.practicum.filmorate.validators.NoSpaceInString;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Past;
import java.time.LocalDate;
import java.util.HashMap;


@Data
@Table(name = "courses_type")
public class User {
    @Id
    @Column(value = "user_id")
    private int id;
    @Email(message = "EMAIL IS INCORRECT")
    @NotEmpty
    @Column(value = "email")
    private String email;
    @NotEmpty(message = "LOGIN IS EMPTY")
    @NoSpaceInString(message = "STRING CONTAIN SPACE")
    @Column(value = "login")
    private String login;
    @Column(value = "name")
    private String name;
    @Past(message = "DATE IS INCORRECT")
    @Column(value = "birthday")
    private LocalDate birthday;
    private HashMap<Integer, Boolean> friends = new HashMap<>();

    public User(String email, String login, String name, LocalDate birthday) {
        this.email = email;
        this.login = login;
        this.name = name;
        this.birthday = birthday;
    }

    public void addFriend(int userId, boolean mutually) {
        friends.put(userId, mutually);
    }

    public void deleteFriend(int userId) {
        friends.remove(userId);
    }

}
