package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class FilmorateApplicationTests {

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

		var response = this.mockMvc.perform(post("/users")
				.content(requestBody)
				.contentType(MediaType.APPLICATION_JSON));

		response
				//.andDo(MockMvcResultHandlers.print())
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
		ArrayList<User> allUsers = new ArrayList<>();
		user.setId(1);
		user2.setId(2);
		allUsers.add(user);
		allUsers.add(user2);


		var response = this.mockMvc.perform(get("/users"));
		response
				//.andDo(MockMvcResultHandlers.print())
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(allUsers)));

	}

	@Test
	@Order(3)
	void createNewUserWithSpaceInLoginExpectingException() throws Exception {
		User user = new User("aa@bb.com", "lo gin", "name", BIRTHDAY_DATE);
		String requestBody = objectMapper.writeValueAsString(user);

		try {
			this.mockMvc.perform(post("/users")
					.content(requestBody)
					.contentType(MediaType.APPLICATION_JSON)).andExpect(mvcResult
					-> mvcResult.getResolvedException().getClass().equals(ValidationException.class));
		} catch (ValidationException e) {
			System.out.println("all ok");
		}
	}
}