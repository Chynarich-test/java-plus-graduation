package ru.yandex.practicum.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

@Getter
@Setter
@ToString
public class UserAction {
    private Long userId;
    private Long eventId;
    private ActionType actionType;
    private Instant timestamp;

}
