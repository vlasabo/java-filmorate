package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.model.User;
import java.time.LocalDate;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UserControllerTests {

    private final static LocalDate BIRTHDAY_DATE = LocalDate.ofEpochDay(1);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @SpyBean
    private UserController userController;


    @Test
    @Order(1)
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
    @Order(2)
    void createTwoNewUsersAndCompareWithListFromControllerReceivedRequestGet() throws Exception {
        User user = new User("aa@bb.com", "login", "name", BIRTHDAY_DATE.plusDays(1));
        String requestBody = objectMapper.writeValueAsString(user);
        this.mockMvc.perform(post("/users").content(requestBody).contentType(MediaType.APPLICATION_JSON));

        User user2 = new User("aa@bb2.com", "login2", "name2", BIRTHDAY_DATE.plusDays(2));
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
        User user = new User("aa@bb.com", "", "name", BIRTHDAY_DATE);
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


}

