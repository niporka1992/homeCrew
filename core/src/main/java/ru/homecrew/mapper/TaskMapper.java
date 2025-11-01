package ru.homecrew.mapper;

import java.util.Comparator;
import java.util.List;
import org.mapstruct.*;
import org.springframework.lang.Nullable;
import ru.homecrew.dto.task.TaskCreateDto;
import ru.homecrew.dto.task.TaskDto;
import ru.homecrew.entity.AppUser;
import ru.homecrew.entity.task.Task;
import ru.homecrew.enums.TaskStatus;

@Mapper(
        componentModel = "spring",
        imports = {TaskStatus.class})
public interface TaskMapper {
    /** Создание новой сущности задачи со статусом NEW. */
    @Mapping(target = "status", expression = "java(TaskStatus.NEW)")
    @Mapping(target = "assignee", ignore = true)
    Task toNewEntity(TaskCreateDto dto);

    /** Преобразование задачи в DTO для выдачи наружу. */
    @Mapping(target = "assigneeFullName", expression = "java(getAssigneeFullName(entity.getAssignee()))")
    @Mapping(target = "dateOfCreate", source = "createdAt")
    @Mapping(target = "title", ignore = true)
    TaskDto toDto(Task entity);

    /**
     * Маппинг списка задач в список DTO с обратной сортировкой по дате создания (новые — первыми).
     */
    default List<TaskDto> toDtoListSortedDesc(@Nullable List<Task> entities) {
        if (entities == null || entities.isEmpty()) return List.of();
        return entities.stream()
                .sorted(Comparator.comparing(Task::getCreatedAt).reversed())
                .map(this::toDto)
                .toList();
    }

    /** Маппинг списка задач в список DTO. */
    default List<TaskDto> toDtoList(@Nullable List<Task> entities) {
        return entities == null ? List.of() : entities.stream().map(this::toDto).toList();
    }

    /** Обновление существующей задачи из DTO без перезаписи связей. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "assignee", ignore = true)
    @Mapping(target = "history", ignore = true)
    void updateEntityFromDto(TaskCreateDto dto, @MappingTarget Task entity);

    /** Безопасное получение ФИО исполнителя. */
    default String getAssigneeFullName(AppUser assignee) {
        return assignee == null ? null : assignee.getFullName();
    }
}
