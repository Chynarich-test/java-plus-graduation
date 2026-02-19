package ru.yandex.practicum.mapper;

import org.mapstruct.*;
import ru.yandex.practicum.dto.CommentDto;
import ru.yandex.practicum.model.Comment;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "authorId", source = "comment.authorId")
    @Mapping(target = "authorName", source = "authorName")
    @Mapping(target = "eventId", source = "comment.eventId")
    CommentDto toDto(Comment comment, String authorName);

    @Mapping(target = "authorId", source = "comment.authorId")
    @Mapping(target = "authorName", ignore = true)
    @Mapping(target = "eventId", source = "comment.eventId")
    CommentDto toDto(Comment comment);
}