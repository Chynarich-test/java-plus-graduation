package ru.yandex.practicum.repository;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.JoinType;
import ru.yandex.practicum.model.Comment;

public class CommentSpecs {

    public static Specification<Comment> byEventId(Long eventId) {
        return (root, query, cb) -> cb.equal(root.get("event").get("id"), eventId);
    }

    public static Specification<Comment> byAuthorId(Long authorId) {
        return (root, query, cb) -> cb.equal(root.get("author").get("id"), authorId);
    }

    public static Specification<Comment> notDeleted() {
        return (root, query, cb) -> cb.isFalse(root.get("isDeleted"));
    }

    public static Specification<Comment> fetchAll() {
        return (root, query, cb) -> {
            if (Comment.class.equals(query.getResultType())) {
                root.fetch("author", JoinType.LEFT);
                root.fetch("event", JoinType.LEFT);
                query.distinct(true);
            }
            return cb.conjunction();
        };
    }
}