package ru.homecrew.dto.task;

import java.time.LocalDateTime;
import java.util.List;
import ru.homecrew.enums.TaskActionType;
import ru.homecrew.enums.TaskStatus;

public record TaskHistoryDto(
        Long id,
        String actorFullName,
        TaskActionType actionType,
        String details,
        LocalDateTime createdAt,
        List<TaskCommentDto> comments,
        List<TaskMediaDto> attachments,
        TaskStatus statusAfter) {}
