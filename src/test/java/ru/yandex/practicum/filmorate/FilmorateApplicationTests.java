package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class FilmorateApplicationTests {

	private final LocalDate BIRTHDAY_DATE = LocalDate.ofEpochDay(1);

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@SpyBean
	private UserController userController;


	@Test
	@Order(1)
	void givenNewUserWithNullName_whenCreated_thenAddsUserWithNameEqualsLogin() throws Exception {
		User user = new User("aa@bb.com", "login", null, BIRTHDAY_DATE);
		String requestBody = objectMapper.writeValueAsString(user);

		var response = this.mockMvc.perform(post("/users")
				.content(requestBody)
				.contentType(MediaType.APPLICATION_JSON));

		response.andDo(MockMvcResultHandlers.print())
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1))
				.andExpect(MockMvcResultMatchers.jsonPath("$.email").value("aa@bb.com"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.login").value("login"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.name").value("login"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.birthday").value(BIRTHDAY_DATE.toString()));
	}
}