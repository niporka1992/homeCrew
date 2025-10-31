package ru.homecrew.dto.scheduler.type;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.time.LocalDate;
import java.time.LocalTime;

@JsonTypeName("SIMPLE")
public record SimpleScheduleDto(
        String jobName,
        LocalDate startDate,
        LocalTime startTime,
        LocalDate endDate,
        LocalTime endTime,
        Integer repeatCount,
        Long repeatIntervalMs)
        implements ScheduleDto {}
