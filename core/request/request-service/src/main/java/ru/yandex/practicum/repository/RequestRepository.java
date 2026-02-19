package ru.yandex.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.dto.ConfirmedRequestCount;
import ru.yandex.practicum.model.Request;
import ru.yandex.practicum.dto.RequestStatus;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long>, JpaSpecificationExecutor<Request> {

    default List<Request> findAllByEventIdWithRelations(Long eventId) {
        return findAll(RequestSpecs.fetchRequesterAndEvent()
                .and(RequestSpecs.byEvent(eventId)));
    }

    default List<Request> findAllByRequesterId(Long requesterId) {
        return findAll(RequestSpecs.fetchRequesterAndEvent()
                .and(RequestSpecs.byRequester(requesterId)));
    }

    default long countConfirmedRequests(Long eventId) {
        return count(RequestSpecs.byEvent(eventId)
                .and(RequestSpecs.byStatus(RequestStatus.CONFIRMED)));
    }

    @Query("""
            SELECT r FROM Request r
            WHERE r.eventId = :eventId AND r.id IN :requestIds
            """)

    List<Request> findByEventIdAndIdInWithRelations(@Param("eventId") Long eventId, @Param("requestIds") List<Long> requestIds);

    @Query("""
            SELECT new ru.yandex.practicum.dto.ConfirmedRequestCount(r.eventId, COUNT(r))
            FROM Request r
            WHERE r.status = :status
                AND r.eventId IN :eventIds
            GROUP BY r.eventId
            """)
    List<ConfirmedRequestCount> countConfirmedRequestsForEvents(@Param("eventIds") List<Long> eventIds,
                                                                @Param("status") RequestStatus status);

    boolean existsByEventIdAndRequesterId(Long eventId, Long requesterId);
}