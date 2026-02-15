package ru.yandex.practicum.mapper;

import org.mapstruct.*;
import ru.yandex.practicum.dto.CompilationDto;
import ru.yandex.practicum.dto.NewCompilationDto;
import ru.yandex.practicum.event.dto.EventFullDto;
import ru.yandex.practicum.model.Compilation;

import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CompilationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eventIds", source = "events")
    Compilation toEntity(NewCompilationDto dto);

    @Mapping(target = "id", source = "compilation.id")
    @Mapping(target = "title", source = "compilation.title")
    @Mapping(target = "pinned", source = "compilation.pinned")
    @Mapping(target = "events", source = "events")
    CompilationDto toDto(Compilation compilation, List<EventFullDto> events);

    List<CompilationDto> toDtoList(List<Compilation> compilations);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eventIds", source = "events")
    void updateCompilationFromDto(NewCompilationDto dto, @MappingTarget Compilation entity);

    @Named("emptyListIfNull")
    default List<EventFullDto> emptyListIfNull(List<EventFullDto> events) {
        return events != null ? events : Collections.emptyList();
    }
}