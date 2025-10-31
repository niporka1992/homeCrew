package ru.homecrew.dto.task;

import java.time.LocalDateTime;
import java.util.List;
import ru.homecrew.enums.TaskStatus;

public record TaskDetailsDto(
        Long id,
        String description,
        String assigneeFullName,
        TaskStatus status,
        LocalDateTime dateOfCreate,
        List<TaskHistoryDto> history) {}
