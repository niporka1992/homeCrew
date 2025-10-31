package ru.homecrew.dto.scheduler.type;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.time.LocalDate;
import java.time.LocalTime;

@JsonTypeName("CUSTOM")
public record CustomScheduleDto(
        String jobName,
        LocalDate startDate,
        LocalTime startTime,
        LocalDate endDate,
        LocalTime endTime,
        String customCron)
        implements ScheduleDto {}
