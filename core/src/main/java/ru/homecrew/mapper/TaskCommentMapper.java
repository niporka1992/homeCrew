package ru.homecrew.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.homecrew.dto.task.TaskCommentDto;
import ru.homecrew.entity.task.TaskComment;

@Mapper(componentModel = "spring")
public interface TaskCommentMapper {

    @Mapping(target = "authorName", source = "author.username")
    TaskCommentDto toDto(TaskComment entity);
}
