package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.IFilmService;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.yandex.practicum.filmorate.testutil.TestConstants.LONG_DESCRIPTION;
import static ru.yandex.practicum.filmorate.testutil.TestConstants.THRESHOLD_DATE;

@WebMvcTest(FilmController.class)
class FilmControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IFilmService service;

    @ParameterizedTest
    @MethodSource("invalidFilms")
    void whenCreateInvalidFilm_status400(Film film) throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @MethodSource("invalidFilms")
    void whenUpdateInvalidFilm_status400(Film film) throws Exception {
        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }

    private static Stream<Film> invalidFilms() {
        return Stream.of(
                Film // null name
                        .builder()
                        .description("Description")
                        .releaseDate(LocalDate.now())
                        .duration(10)
                        .build(),
                Film // long description
                        .builder()
                        .name("Name")
                        .description(LONG_DESCRIPTION)
                        .releaseDate(LocalDate.now())
                        .duration(10)
                        .build(),
                Film // negative duration
                        .builder()
                        .name("Name")
                        .description("Description")
                        .releaseDate(LocalDate.now())
                        .duration(-10)
                        .build(),
                Film // invalid release date
                        .builder()
                        .name("Name")
                        .description("Description")
                        .releaseDate(THRESHOLD_DATE)
                        .duration(10)
                        .build(),
                Film // all fields incorrect
                        .builder()
                        .name("")
                        .description(LONG_DESCRIPTION)
                        .releaseDate(THRESHOLD_DATE)
                        .duration(-10)
                        .build(),
                null
        );
    }

}