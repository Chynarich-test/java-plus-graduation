package ru.yandex.practicum.common.dsl.exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.common.dsl.exception.dto.ApiError;

import java.time.LocalDateTime;
import java.util.Objects;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(final NotFoundException e) {
        return ApiError.builder()
                .reason("The required object was not found.")
                .message(e.getMessage())
                .status(HttpStatus.NOT_FOUND.name())
                .timestamp(LocalDateTime.now())
                .errorCode("OBJECT_NOT_FOUND")
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleValidation(final ValidationException e) {
        return ApiError.builder()
                .reason("Integrity constraint has been violated.")
                .message(e.getMessage())
                .status(HttpStatus.CONFLICT.name())
                .timestamp(LocalDateTime.now())
                .errorCode("VALIDATION_ERROR")
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleExist(final ExistException e) {
        return ApiError.builder()
                .reason("Integrity constraint has been violated.")
                .message(e.getMessage())
                .status(HttpStatus.CONFLICT.name())
                .timestamp(LocalDateTime.now())
                .errorCode("ENTITY_ALREADY_EXISTS")
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflict(final ConflictException e) {
        return ApiError.builder()
                .reason("Integrity constraint has been violated.")
                .message(e.getMessage())
                .status(HttpStatus.CONFLICT.name())
                .timestamp(LocalDateTime.now())
                .errorCode("CONFLICT_CONDITION")
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBadRequest(final MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError == null ? e.getMessage()
                : String.format("Field: %s. Error: %s. Value: %s",
                fieldError.getField(),
                fieldError.getDefaultMessage(),
                Objects.toString(fieldError.getRejectedValue(), "null"));

        return ApiError.builder()
                .reason("Incorrectly made request.")
                .message(message)
                .status(HttpStatus.BAD_REQUEST.name())
                .timestamp(LocalDateTime.now())
                .errorCode("ARGUMENT_NOT_VALID")
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleInvalidDateRange(final InvalidDateRangeException e) {
        return ApiError.builder()
                .reason("Incorrectly made request.")
                .message(e.getMessage())
                .status(HttpStatus.BAD_REQUEST.name())
                .timestamp(LocalDateTime.now())
                .errorCode("INVALID_DATE_RANGE")
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBadJson(final HttpMessageNotReadableException e) {
        return ApiError.builder()
                .reason("Incorrectly made request.")
                .message(e.getMessage())
                .status(HttpStatus.BAD_REQUEST.name())
                .timestamp(LocalDateTime.now())
                .errorCode("MALFORMED_JSON")
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMissingRequestParam(final MissingServletRequestParameterException e) {
        return ApiError.builder()
                .reason("Incorrectly made request.")
                .message("Отсутствует обязательный параметр запроса: " + e.getParameterName())
                .status(HttpStatus.BAD_REQUEST.name())
                .timestamp(LocalDateTime.now())
                .errorCode("MISSING_PARAMETER")
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleOther(final Exception e) {
        return ApiError.builder()
                .reason("Unexpected error occurred.")
                .message(e.getMessage())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.name())
                .timestamp(LocalDateTime.now())
                .errorCode("INTERNAL_SERVER_ERROR")
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ApiError handleExplicit503(ServiceUnavailableException e) {
        return ApiError.builder()
                .reason("SERVICE_UNAVAILABLE")
                .message(e.getMessage())
                .status(HttpStatus.SERVICE_UNAVAILABLE.name())
                .timestamp(LocalDateTime.now())
                .errorCode("SERVICE_UNAVAILABLE")
                .build();
    }
}