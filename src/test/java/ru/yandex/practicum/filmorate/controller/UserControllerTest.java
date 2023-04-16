package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.IUserService;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IUserService service;

    @ParameterizedTest
    @MethodSource("invalidUsers")
    void whenCreateInvalidUser_status400(User user) throws Exception {
        mockMvc
                .perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user))
                )
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @MethodSource("invalidUsers")
    void whenUpdateInvalidUser_status400(User user) throws Exception {
        mockMvc
                .perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user))
                )
                .andExpect(status().isBadRequest());
    }

    private static Stream<User> invalidUsers() {
        return Stream.of(
                User // invalid email
                        .builder()
                        .email("email.email")
                        .login("login")
                        .name("name")
                        .birthday(LocalDate.now().minusDays(10))
                        .build(),
                User // invalid login
                        .builder()
                        .email("email@email.com")
                        .login("invalid login")
                        .name("name")
                        .birthday(LocalDate.now().minusDays(10))
                        .build(),
                User // invalid birthDay
                        .builder()
                        .email("email@emai.com")
                        .login("login")
                        .name("name")
                        .birthday(LocalDate.now().plusDays(1))
                        .build(),
                User
                        .builder()
                        .email("incorrect email")
                        .login("incorrect login")
                        .name("name")
                        .birthday(LocalDate.now().plusDays(10))
                        .build(),
                null
        );
    }

}