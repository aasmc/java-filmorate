package ru.yandex.practicum.filmorate.exception;

import lombok.Builder;

import java.util.List;

@Builder
public class ErrorResponse {
    private String message;
    private int code;
    private List<String> fieldErrors;
}
