package ru.yandex.practicum.filmorate.exception;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class ErrorResponse {
    private String message;
    private int code;
    private List<String> fieldErrors;
}
