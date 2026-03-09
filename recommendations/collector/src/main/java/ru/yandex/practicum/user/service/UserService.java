package ru.yandex.practicum.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.yandex.practicum.user.kafka.UserActionPublisher;
import ru.yandex.practicum.user.mapper.UserActionAvroMapper;
import ru.yandex.practicum.user.model.UserAction;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserActionPublisher userActionPublisher;
    private final UserActionAvroMapper userActionAvroMapper;

    public void collectUserAction(UserAction userAction){
        UserActionAvro userActionAvro = userActionAvroMapper.toAvro(userAction);

        userActionPublisher.publish(userActionAvro);
    }
}
