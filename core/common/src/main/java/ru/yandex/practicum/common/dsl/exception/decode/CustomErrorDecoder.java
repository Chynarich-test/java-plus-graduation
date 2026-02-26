package ru.yandex.practicum.common.dsl.exception.decode;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StreamUtils;
import ru.yandex.practicum.common.dsl.exception.*;
import ru.yandex.practicum.common.dsl.exception.dto.ApiError;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@RequiredArgsConstructor
public class CustomErrorDecoder implements ErrorDecoder {
    private final ObjectMapper objectMapper;
    private final ErrorDecoder defaultDecoder = new Default();
    @Override
    public Exception decode(String methodKey, Response response) {
        String message = null;
        String rawBody = "";
        ApiError apiError = null;

        try {
            if (response.body() != null) {
                rawBody = StreamUtils.copyToString(response.body().asInputStream(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            log.error("Ошибка чтения тела ответа от {}", methodKey, e);
            return defaultDecoder.decode(methodKey, response);
        }

        if (!rawBody.isEmpty()) {
            try {
                apiError = objectMapper.readValue(rawBody, ApiError.class);
                message = apiError.getMessage();
            } catch (Exception e) {
                log.warn("Не удалось распарсить ApiError: {}", rawBody);
                message = rawBody;
            }
        }

        if(apiError != null) {
            return switch (apiError.getErrorCode()) {
                case "OBJECT_NOT_FOUND" -> new NotFoundException(message);

                case "VALIDATION_ERROR" -> new ValidationException(message);

                case "ENTITY_ALREADY_EXISTS" -> new ExistException(message);

                case "CONFLICT_CONDITION" -> new ConflictException(message);

                case "INVALID_DATE_RANGE" -> new InvalidDateRangeException(message);

                case "SERVICE_UNAVAILABLE" -> new ServiceUnavailableException(message);

                case "ARGUMENT_NOT_VALID", "MALFORMED_JSON", "MISSING_PARAMETER" ->
                        new ValidationException("Bad Request: " + message);

                case "INTERNAL_SERVER_ERROR" -> new RuntimeException("Server Error: " + message);

                default -> {
                    log.warn("Unknown ErrorCode: {}", apiError.getErrorCode());
                    yield new RuntimeException("Unknown error: " + message);
                }
            };
        }

        if (response.status() == 503) {
            return new ServiceUnavailableException("SERVICE_UNAVAILABLE: " + message);
        }


        return feign.FeignException.errorStatus(methodKey,
                response.toBuilder().body(rawBody, StandardCharsets.UTF_8).build());

    }
}
