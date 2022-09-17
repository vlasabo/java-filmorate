package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FilmController.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class FilmControllerTests {

    private final static LocalDate FIRST_FILM_RELEASE_DAY = LocalDate.of(1895, 12, 28);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @SpyBean
    private FilmController filmController;

    @SpyBean
    private UserController userController;

    @SpyBean
    private UserService userService;

    @SpyBean
    private InMemoryUserStorage userStorage;

    @SpyBean
    private FilmService filmService;

    @SpyBean
    private InMemoryFilmStorage filmStorage;


    @Test
    void createTwoNewFilmsAndCompareWithListFromControllerReceivedRequestGet() throws Exception {
        Film film = new Film("filmName", "descr1", FIRST_FILM_RELEASE_DAY.plusDays(1), 100);
        String requestBody = objectMapper.writeValueAsString(film);
        this.mockMvc.perform(post("/films").content(requestBody).contentType(MediaType.APPLICATION_JSON));

        Film film2 = new Film("filmName", "descr2", FIRST_FILM_RELEASE_DAY.plusDays(1), 100);
        requestBody = objectMapper.writeValueAsString(film2);
        this.mockMvc.perform(post("/films").content(requestBody).contentType(MediaType.APPLICATION_JSON));

        var response = this.mockMvc.perform(get("/films")).andExpect(status().isOk())
                .andReturn().getResponse();
        String answer = response.getContentAsString();
        Assertions.assertEquals(answer, objectMapper.writeValueAsString(filmController.getAllFilms()));
    }

    @Test
    void createNewFilmWithEmptyOrNullTitleExpectingStatus400() throws Exception {
        Film film = new Film("", "", FIRST_FILM_RELEASE_DAY.plusDays(1), 100);
        String requestBody = objectMapper.writeValueAsString(film);
        this.mockMvc.perform(post("/films").content(requestBody).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

        Film film2 = new Film(null, "", FIRST_FILM_RELEASE_DAY.plusDays(2), 100);
        requestBody = objectMapper.writeValueAsString(film2);

        this.mockMvc.perform(post("/films").content(requestBody).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void createNewFilmWithDescriptionLengthOver200ExpectingStatus400() throws Exception {
        String forDescriprion = "0123456789";
        StringBuilder sb = new StringBuilder(forDescriprion);
        sb.append(forDescriprion.repeat(19));
        var correctDescr = sb.toString();
        sb.append("0");//201
        var incorrectDescr = sb.toString();
        Film film = new Film("title", incorrectDescr, FIRST_FILM_RELEASE_DAY.plusDays(1), 100);
        String requestBody = objectMapper.writeValueAsString(film);
        this.mockMvc.perform(post("/films").content(requestBody).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

        Film film2 = new Film("title", correctDescr, FIRST_FILM_RELEASE_DAY.plusDays(1), 100);
        requestBody = objectMapper.writeValueAsString(film2);
        this.mockMvc.perform(post("/films").content(requestBody).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());


    }

    @Test
    void createNewFilmWithIncorrectReleaseDayExpectingStatus400() throws Exception {
        Film film = new Film("title", "", FIRST_FILM_RELEASE_DAY.minusDays(1), 100);
        String requestBody = objectMapper.writeValueAsString(film);

        this.mockMvc.perform(post("/films").content(requestBody).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void createNewFilmWithNegativeOrZeroDurationExpectingStatus400() throws Exception {
        Film film = new Film("title", "", FIRST_FILM_RELEASE_DAY, -1);
        String requestBody = objectMapper.writeValueAsString(film);
        this.mockMvc.perform(post("/films").content(requestBody).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

        Film film2 = new Film("title", "", FIRST_FILM_RELEASE_DAY, 0);
        requestBody = objectMapper.writeValueAsString(film2);
        this.mockMvc.perform(post("/films").content(requestBody).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

        Film film3 = new Film("title", "", FIRST_FILM_RELEASE_DAY, 1);
        requestBody = objectMapper.writeValueAsString(film3);
        this.mockMvc.perform(post("/films").content(requestBody).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void PutMethodWithIncorrectReleaseDateExpectingStatus400() throws Exception {
        Film film = new Film("title", "", FIRST_FILM_RELEASE_DAY, 1);
        String requestBody = objectMapper.writeValueAsString(film);

        this.mockMvc.perform(post("/films")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        film.setReleaseDate(FIRST_FILM_RELEASE_DAY.minusDays(1));
        requestBody = objectMapper.writeValueAsString(film);
        this.mockMvc.perform(put("/films")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void PutMethodWithCorrectAndIncorrectIdExpectingStatus400() throws Exception {
        Film film = new Film("title", "", FIRST_FILM_RELEASE_DAY, 1);
        String requestBody = objectMapper.writeValueAsString(film);

        this.mockMvc.perform(post("/films")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        film.setId(1);
        film.setDescription("abirvalg!");
        requestBody = objectMapper.writeValueAsString(film);
        this.mockMvc.perform(put("/films")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        film.setId(100);
        requestBody = objectMapper.writeValueAsString(film);
        this.mockMvc.perform(put("/films")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

}
