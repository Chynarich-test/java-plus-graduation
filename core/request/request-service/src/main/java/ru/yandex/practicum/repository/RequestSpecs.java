package ru.yandex.practicum.repository;

import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import ru.yandex.practicum.model.Request;
import ru.yandex.practicum.dto.RequestStatus;


public class RequestSpecs {

    public static Specification<Request> byEvent(Long eventId) {
        return (root, query, cb) -> cb.equal(root.get("eventId"), eventId);
    }

    public static Specification<Request> byRequester(Long requesterId) {
        return (root, query, cb) -> cb.equal(root.get("requesterId"), requesterId);
    }

    public static Specification<Request> byStatus(RequestStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Request> byStatuses(Iterable<RequestStatus> statuses) {
        return (root, query, cb) -> root.get("status").in(statuses);
    }

    public static Specification<Request> fetchRequesterAndEvent() {
        return (root, query, cb) -> {
            return cb.conjunction();
        };
    }

    public static Specification<Request> byEventAndIds(Long eventId, Iterable<Long> ids) {
        return (root, query, cb) ->
                cb.and(
                        cb.equal(root.get("eventId"), eventId),
                        root.get("id").in(ids)
                );
    }
}