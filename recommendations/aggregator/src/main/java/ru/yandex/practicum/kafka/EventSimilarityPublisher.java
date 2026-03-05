package ru.yandex.practicum.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;


@Component
@RequiredArgsConstructor
@Slf4j
public class EventSimilarityPublisher {
    private final KafkaTemplate<String, SpecificRecordBase> kafkaTemplate;

    @Value("${kafka.topic.event.similarity}")
    private String topic;

    public void publish(EventSimilarityAvro data) {
        String key = data.getEventA() + "-" + data.getEventB();
        long timestamp = data.getTimestamp().toEpochMilli();

        ProducerRecord<String, SpecificRecordBase> record =
                new ProducerRecord<>(topic, null, timestamp, key, data);

        kafkaTemplate.send(record).whenComplete((result, ex) -> {
            if (ex == null) {
                log.debug("Успешная отправка. Topic: {}, Partition: {}, Offset: {}",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Ошибка при отправке в Kafka. Topic: {}, Key: {}", topic, key, ex);
            }
        });
    }
}
