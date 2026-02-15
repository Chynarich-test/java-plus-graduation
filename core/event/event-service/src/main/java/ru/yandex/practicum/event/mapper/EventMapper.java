package ru.yandex.practicum.event.mapper;

import org.mapstruct.*;
import ru.yandex.practicum.category.mapper.CategoryMapper;
import ru.yandex.practicum.category.model.Category;
import ru.yandex.practicum.dto.UserShortDto;
import ru.yandex.practicum.event.dto.*;
import ru.yandex.practicum.event.model.Event;

import java.util.List;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class})
public interface EventMapper {

    @Mapping(source = "event.eventDate", target = "eventDate", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(target = "initiator", source = "userShortDto")
    @Mapping(source = "event.id", target = "id")
    EventFullDto toEventFullDto(Event event, UserShortDto userShortDto);

    @Mapping(source = "event.id", target = "id")
    @Mapping(target = "initiator", source = "userShortDto")
    EventShortDto toEventShortDto(Event event, UserShortDto userShortDto);

    @Mapping(source = "eventDate", target = "eventDate", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(target = "initiator.id", source = "initiatorId")
    EventFullDto toEventFullDto(Event event);

    @Mapping(target = "category", source = "category", qualifiedByName = "mapCategoryIdToCategory")
    Event fromNewEventDto(NewEventDto dto);

    List<EventShortDto> toEventsShortDto(List<Event> events);

    @Mapping(target = "category", source = "category", qualifiedByName = "mapCategoryIdToCategory")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEventFromUserDto(UpdateEventUserRequest dto, @MappingTarget Event entity);

    @Mapping(target = "category", source = "category", qualifiedByName = "mapCategoryIdToCategory")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEventFromAdminDto(UpdateEventAdminRequest dto, @MappingTarget Event entity);

    List<EventFullDto> toEventsFullDto(List<Event> events);

    @Named("mapCategoryIdToCategory")
    default Category mapCategoryIdToCategory(Long id) {
        if (id == null) return null;
        ru.yandex.practicum.category.model.Category category = new ru.yandex.practicum.category.model.Category();
        category.setId(id);
        return category;
    }
}
