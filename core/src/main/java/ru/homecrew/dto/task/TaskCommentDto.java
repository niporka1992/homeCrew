package ru.homecrew.dto.task;

import java.time.LocalDateTime;

public record TaskCommentDto(Long id, String authorName, String text, LocalDateTime createdAt) {}
