package ru.homecrew.dto.task;

import java.time.LocalDateTime;

public record TaskMediaDto(Long id, String fileUrl, LocalDateTime createdAt) {}
