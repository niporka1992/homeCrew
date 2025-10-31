package ru.homecrew.dto.scheduler.type;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.time.LocalDate;
import java.time.LocalTime;

@JsonTypeName("DAILY")
public record DailyScheduleDto(
        String jobName, LocalDate startDate, LocalTime startTime, LocalDate endDate, LocalTime endTime)
        implements ScheduleDto {}
