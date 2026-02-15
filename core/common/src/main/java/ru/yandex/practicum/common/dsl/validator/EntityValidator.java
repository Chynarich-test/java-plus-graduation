package ru.yandex.practicum.common.dsl.validator;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.common.dsl.exception.NotFoundException;

@Component
public class EntityValidator {
    public <T> T ensureAndGet(JpaRepository<T, Long> repository, Long id, String entityName) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(entityName + " не найден: id=" + id));
    }

    public void ensureExists(JpaRepository<?, Long> repository, Long id, String entityName) {
        if (!repository.existsById(id)) {
            throw new NotFoundException(entityName + " не найден: id=" + id);
        }
    }
}