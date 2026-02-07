package ru.yandex.practicum.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentAdminFilter {

    private Long eventId;
    private Long authorId;
    private Boolean includeDeleted = false;
    private Integer from = 0;
    private Integer size = 10;
}