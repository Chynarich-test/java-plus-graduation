package practicum.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import practicum.service.AnalyzerService;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

@Component
@Slf4j
@RequiredArgsConstructor
public class Listener {
    private final AnalyzerService service;

    @KafkaListener(topics = "${kafka.topic.user.action}", groupId = "${kafka.group.id}",
            containerFactory = "userActionContainerFactory")
    public void listenUserAction(UserActionAvro message) {
        try {
            log.debug("Получено сообщение: {}", message);
            service.getUserAction(message);
        } catch (Exception e) {
            log.error("Ошибка при обработке сообщения: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "${kafka.topic.event.similarity}", groupId = "${kafka.group.id}",
            containerFactory = "eventSimilarityContainerFactory")
    public void listenEventSimilarity(EventSimilarityAvro message) {
        try {
            log.debug("Получено сообщение: {}", message);
            service.getEventSimilarity(message);
        } catch (Exception e) {
            log.error("Ошибка при обработке сообщения: {}", e.getMessage());
        }
    }
}
