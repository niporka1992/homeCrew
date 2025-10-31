package ru.homecrew.dto.task;

import java.time.LocalDateTime;
import ru.homecrew.enums.TaskStatus;

public record TaskDto(
        Long id,
        String title,
        String description,
        TaskStatus status,
        String assigneeFullName,
        LocalDateTime dateOfCreate) {}
