package ru.homecrew.dto.scheduler.jobs;

import java.time.LocalDateTime;

public record JobInfoDto(
        String name, String description, String status, LocalDateTime previousFireTime, LocalDateTime nextFireTime) {}
