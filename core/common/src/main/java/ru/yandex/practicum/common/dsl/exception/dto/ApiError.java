package ru.yandex.practicum.common.dsl.exception.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiError {
    private String reason;
    private String message;
    private String status;
    private LocalDateTime timestamp;
    private String errorCode;
}