package ru.yandex.practicum.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.yandex.practicum.service.SimilarityService;

@Component
@Slf4j
@RequiredArgsConstructor
public class Listener {
    private final SimilarityService service;

    //Нас конечно не учили такой аннотации, но вручную коммитить офсеты в бесконечном цикле как будто для моей задачи излишно
    @KafkaListener(topics = "${kafka.topic.user.action}", groupId = "${kafka.group.id}")
    public void listen(UserActionAvro message) {
        try {
            log.debug("Получено сообщение: {}", message);
            service.recalculateSimilarity(message);
        } catch (Exception e) {
            log.error("Ошибка при обработке сообщения: {}", e.getMessage());
        }
    }
}
