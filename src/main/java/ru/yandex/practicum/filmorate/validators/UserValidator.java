package ru.yandex.practicum.filmorate.validators;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exception.MyValidationException;
import ru.yandex.practicum.filmorate.model.User;

@Slf4j
public class UserValidator {

    public void validateUser(User user){
        if (user.getLogin().contains(" ")) {
            log.debug("incorrect adding user {}", user);
            throw new MyValidationException("incorrect login");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }


}
