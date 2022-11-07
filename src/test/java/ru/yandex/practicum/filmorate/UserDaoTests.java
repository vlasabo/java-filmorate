package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserDaoTests {


    @Autowired
    private UserStorage userStorage;

    @Test
    public void testFindUserById() {
        addUser(1);
        Optional<User> userOptional = userStorage.findUserById(1);

        assertThat(userOptional).isPresent().hasValueSatisfying(userFromStorage -> assertThat(userFromStorage).hasFieldOrPropertyWithValue("id", 1));
    }

    @Test
    public void testFindUserByNotRealId() {
        Optional<User> userOptional999 = userStorage.findUserById(999);
        Assertions.assertEquals(userOptional999, Optional.empty());
    }

    @Test
    public void testFindUserWithIncorrectIdExpectStatus404() {
        Optional<User> userOptional = userStorage.findUserById(999);

        assertThat(userOptional).isEmpty();
    }

    @Test
    public void testUsersUpdate() {
        addUser(1);
        User user2 = addUser(2);
        user2.setId(1);
        userStorage.updateUser(user2);
        Optional<User> userOptional = userStorage.findUserById(1);

        assertThat(userOptional).isPresent().hasValueSatisfying(userFromStorage ->
                assertThat(userFromStorage).hasFieldOrPropertyWithValue("id", 1)
                        .hasFieldOrPropertyWithValue("login", "login2")
                        .hasFieldOrPropertyWithValue("name", "vladimir2")
                        .hasFieldOrPropertyWithValue("email", "test@test2.com"));
    }

    @Test
    public void testUsersFriendshipAddAndRemoveAndMutually() {
        User user1 = addUser(1);
        User user2 = addUser(2);
        userStorage.updateFriendship(user1, user2, true);
        Optional<User> userOptional = userStorage.findUserById(1);
        Assertions.assertNotEquals(userOptional, Optional.empty());
        Assertions.assertEquals(userOptional.get().getFriends().size(), 1);
        Assertions.assertEquals(userOptional.get().getFriends().get(2), Boolean.TRUE);

        user2 = userStorage.findUserById(2).get();
        user1 = userStorage.findUserById(1).get();
        userStorage.removeFriends(user2, user1);

        Optional<User> userOptional2 = userStorage.findUserById(1);
        Assertions.assertNotEquals(userOptional2, Optional.empty());
        Assertions.assertEquals(userOptional2.get().getFriends().size(), 1);
        Assertions.assertEquals(userOptional2.get().getFriends().get(2), Boolean.FALSE);

        Optional<User> userOptional3 = userStorage.findUserById(2);
        Assertions.assertNotEquals(userOptional3, Optional.empty());
        Assertions.assertEquals(userOptional3.get().getFriends().size(), 0);
    }

    @Test
    public void testGetAllUsers() {
        addUser(1);
        addUser(2);
        List<User> userList = userStorage.getAllUsers();

        Assertions.assertEquals(userList.size(), 2);
    }

    @Test
    public void deleteUserTest() {
        addUser(1);
        Assertions.assertEquals("vladimir1", userStorage.findUserById(1).get().getName());
        userStorage.deleteUser(1);
        Assertions.assertEquals(0, userStorage.getAllUsers().size());
    }
    private User addUser(int i) {
        User user = new User("test@test" + i + ".com", "login" + i, "vladimir" + i, LocalDate.ofEpochDay(1));
        userStorage.addUser(user);
        return user;
    }
} 