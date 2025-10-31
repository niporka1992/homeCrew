package ru.homecrew.dto.task;

import ru.homecrew.enums.TaskTypeTrigger;

public record TaskCreateDto(
        String title, String description, String assigneeUsername, String dueDate, TaskTypeTrigger type) {}
