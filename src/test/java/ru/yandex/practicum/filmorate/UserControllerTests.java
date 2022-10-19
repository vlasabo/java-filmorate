package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UserControllerTests {

    private final static LocalDate BIRTHDAY_DATE = LocalDate.of(1990, 2, 23);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @SpyBean
    private UserController userController;

    @SpyBean
    @Qualifier("userDbStorage")
    private InMemoryUserStorage userStorage;

    @SpyBean
    private UserService userService;


    @Test
    @Order(2)
    void createNewUserWithNullNameAndAddThisUserWithNameEqualsLogin() throws Exception {
        User user = new User("aa@bb.com", "login", null, BIRTHDAY_DATE);
        String requestBody = objectMapper.writeValueAsString(user);

        this.mockMvc.perform(post("/users")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("aa@bb.com"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.login").value("login"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("login"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.birthday").value(BIRTHDAY_DATE.toString()));

    }

    @Test
    @Order(1)
    void createTwoNewUsersAndCompareWithListFromControllerReceivedRequestGet() throws Exception {
        User user = new User("aa@bb.com", "login", "name", BIRTHDAY_DATE);
        String requestBody = objectMapper.writeValueAsString(user);
        this.mockMvc.perform(post("/users").content(requestBody).contentType(MediaType.APPLICATION_JSON));

        User user2 = new User("aa@bb2.com", "login2", "name2", LocalDate.ofEpochDay(1));
        requestBody = objectMapper.writeValueAsString(user2);
        this.mockMvc.perform(post("/users").content(requestBody).contentType(MediaType.APPLICATION_JSON));

        var response = this.mockMvc.perform(get("/users")).andExpect(status().isOk()).andReturn();
        String answer = response.getResponse().getContentAsString();
        Assertions.assertEquals(answer, objectMapper.writeValueAsString(userController.getAllUsers()));
    }

    @Test
    @Order(3)
    void createNewUserWithSpaceInLoginExpectingException() throws Exception {
        User user = new User("aa@bb.com", "log in", "name", BIRTHDAY_DATE);
        String requestBody = objectMapper.writeValueAsString(user);

        this.mockMvc.perform(post("/users")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @Order(4)
    void createNewUserWithEmptyStringOrNullLoginExpectingStatus400() throws Exception {
        User user = new User("aa@bb.com", "", "name", LocalDate.ofEpochDay(1));
        User user2 = new User("aa@bb.com", null, "name", BIRTHDAY_DATE);
        String requestBody = objectMapper.writeValueAsString(user);

        this.mockMvc.perform(post("/users")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

        requestBody = objectMapper.writeValueAsString(user2);

        this.mockMvc.perform(post("/users")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @Order(5)
    void createNewUserWithIncorrectBirthdayDateExpectingStatus400() throws Exception {
        User user = new User("aa@bb.com", "login", "name", LocalDate.now());
        String requestBody = objectMapper.writeValueAsString(user);

        this.mockMvc.perform(post("/users")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

    }

    @Test
    @Order(6)
    void createNewUserWithIncorrectOrEmptyEmailExpectingStatus400() throws Exception {
        User user = new User("aabb", "login", "name", BIRTHDAY_DATE);
        String requestBody = objectMapper.writeValueAsString(user);

        User user2 = new User(null, "login", "name", BIRTHDAY_DATE);
        String requestBody2 = objectMapper.writeValueAsString(user2);

        this.mockMvc.perform(post("/users")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

        this.mockMvc.perform(post("/users")
                        .content(requestBody2)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

    }

    @Test
    @Order(7)
    void createNewUserWithEmptyBodyExpectingStatus400() throws Exception {

        this.mockMvc.perform(post("/users")
                        .content("")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @Order(8)
    void testCorrectPutMethod() throws Exception {
        User user = new User("aa@bb.com", "login", "name", BIRTHDAY_DATE);
        String requestBody = objectMapper.writeValueAsString(user);
        this.mockMvc.perform(post("/users")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        user.setEmail("bb@aa.com");
        user.setId(1);
        requestBody = objectMapper.writeValueAsString(user);
        this.mockMvc.perform(put("/users")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("bb@aa.com"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(9)
    void PutMethodWithIncorrectOrEmptyEmailExpectingStatus400() throws Exception {
        User user = new User("aa@bb.com", "login", "name", BIRTHDAY_DATE);
        String requestBody = objectMapper.writeValueAsString(user);
        this.mockMvc.perform(post("/users")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        user.setEmail("bbaa");
        user.setId(1);
        requestBody = objectMapper.writeValueAsString(user);
        this.mockMvc.perform(put("/users")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @Order(10)
    void PutMethodWithIncorrectBirthdayDateExpectingStatus400() throws Exception {
        User user = new User("aa@bb.com", "login", "name", BIRTHDAY_DATE);
        String requestBody = objectMapper.writeValueAsString(user);
        this.mockMvc.perform(post("/users")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        user.setBirthday(LocalDate.now());
        user.setId(1);
        requestBody = objectMapper.writeValueAsString(user);
        this.mockMvc.perform(put("/users")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @Order(11)
    void putUserWithEmptyBodyExpectingStatus400() throws Exception {
        this.mockMvc.perform(put("/users")
                        .content("")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @Order(12)
    void putUserWithIncorrectIdExpectingStatus400() throws Exception {
        User user = new User("aa@bb.com", "login", "name", BIRTHDAY_DATE);
        String requestBody = objectMapper.writeValueAsString(user);
        this.mockMvc.perform(post("/users")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON));

        user.setId(999);
        requestBody = objectMapper.writeValueAsString(user);
        this.mockMvc.perform(put("/users")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @Order(13)
    void updateUserWithSpaceInLoginExpectingException() throws Exception {
        User user = new User("aa@bb.com", "login", "name", BIRTHDAY_DATE);
        String requestBody = objectMapper.writeValueAsString(user);
        this.mockMvc.perform(post("/users").content(requestBody).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        user.setLogin("lo gin");
        requestBody = objectMapper.writeValueAsString(user);
        this.mockMvc.perform(put("/users")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @Order(14)
    void getUsersFriends() throws Exception {
        User user = new User("aa@bb.com", "login", "name", BIRTHDAY_DATE);
        User user2 = new User("aa@bb2.com", "login2", "name2", BIRTHDAY_DATE);
        User user3 = new User("aa@bb3.com", "login3", "name3", BIRTHDAY_DATE);
        String requestBody = objectMapper.writeValueAsString(user);
        this.mockMvc.perform(post("/users").content(requestBody).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        requestBody = objectMapper.writeValueAsString(user2);
        this.mockMvc.perform(post("/users").content(requestBody).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        requestBody = objectMapper.writeValueAsString(user3);
        this.mockMvc.perform(post("/users").content(requestBody).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        this.mockMvc.perform(put("/users/1/friends/2"));
        this.mockMvc.perform(put("/users/1/friends/3"));

        var response = this.mockMvc.perform(get("/users/1/friends"))
                .andExpect(status().isOk()).andReturn();

        String answer = response.getResponse().getContentAsString();
        var friendsSet = new HashMap<Integer, Boolean>();

        user2.setId(2);
        user3.setId(3);
        friendsSet.put(2, false);
        friendsSet.put(3, false);
        user.setFriends(friendsSet);

        Assertions.assertEquals(answer, objectMapper.writeValueAsString(Stream.of(user2, user3).collect(Collectors.toList())));
    }

    @Test
    @Order(15)
    void deleteFriend() throws Exception {
        User user = new User("aa@bb.com", "login", "name", BIRTHDAY_DATE);
        User user2 = new User("aa@bb2.com", "login2", "name2", BIRTHDAY_DATE);

        String requestBody = objectMapper.writeValueAsString(user);
        this.mockMvc.perform(post("/users").content(requestBody).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        requestBody = objectMapper.writeValueAsString(user2);
        this.mockMvc.perform(post("/users").content(requestBody).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());


        this.mockMvc.perform(put("/users/1/friends/2"));
        var response = this.mockMvc.perform(get("/users/1/friends"))
                .andExpect(status().isOk()).andReturn();
        String answer = response.getResponse().getContentAsString();

        var friendsSet = new HashMap<Integer, Boolean>();
        friendsSet.put(2, false);
        user.setId(1);
        user2.setId(2);
        user.setFriends(friendsSet);

        Assertions.assertEquals(answer, objectMapper.writeValueAsString(Stream.of(user2).collect(Collectors.toList())));

        this.mockMvc.perform(delete("/users/1/friends/2"));
        response = this.mockMvc.perform(get("/users/1/friends"))
                .andExpect(status().isOk()).andReturn();
        answer = response.getResponse().getContentAsString();
        Assertions.assertEquals(answer, "[]");

        response = this.mockMvc.perform(get("/users/2/friends"))
                .andExpect(status().isOk()).andReturn();
        answer = response.getResponse().getContentAsString();
        Assertions.assertEquals(answer, "[]");
    }

    @Test
    @Order(16)
    void getUserById() throws Exception {
        User user = new User("aa@bb.com", "login", "name", BIRTHDAY_DATE);
        String requestBody = objectMapper.writeValueAsString(user);
        this.mockMvc.perform(post("/users").content(requestBody).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        var response = this.mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk()).andReturn();
        String answer = response.getResponse().getContentAsString();

        user.setId(1);
        Assertions.assertEquals(answer, objectMapper.writeValueAsString(user));

        this.mockMvc.perform(get("/users/2"))
                .andExpect(status().is4xxClientError());

    }

    @Test
    @Order(17)
    void getUsersFriendsIntersection() throws Exception {
        User user = new User("aa@bb.com", "login", "name", BIRTHDAY_DATE);
        User user2 = new User("aa@bb2.com", "login2", "name2", BIRTHDAY_DATE);
        User user3 = new User("aa@bb3.com", "login3", "name3", BIRTHDAY_DATE);
        String requestBody = objectMapper.writeValueAsString(user);
        this.mockMvc.perform(post("/users").content(requestBody).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        requestBody = objectMapper.writeValueAsString(user2);
        this.mockMvc.perform(post("/users").content(requestBody).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        requestBody = objectMapper.writeValueAsString(user3);
        this.mockMvc.perform(post("/users").content(requestBody).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        user.setId(1);
        user2.setId(2);
        user3.setId(3);
        this.mockMvc.perform(put("/users/1/friends/2"));
        this.mockMvc.perform(put("/users/3/friends/2"));
        this.mockMvc.perform(put("/users/2/friends/1"));
        this.mockMvc.perform(put("/users/2/friends/3"));

        var setFriends = new HashMap<Integer, Boolean>();
        setFriends.put(1, true);
        setFriends.put(3, true);
        user2.setFriends(setFriends);
        var response = this.mockMvc.perform(get("/users/1/friends/common/3"))
                .andExpect(status().isOk()).andReturn();
        String answer = response.getResponse().getContentAsString();
        Assertions.assertEquals(answer, objectMapper.writeValueAsString(List.of(user2)));
    }

}

