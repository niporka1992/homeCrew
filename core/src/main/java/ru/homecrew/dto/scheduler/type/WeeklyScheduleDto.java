package ru.homecrew.dto.scheduler.type;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@JsonTypeName("WEEKLY")
public record WeeklyScheduleDto(
        String jobName,
        LocalDate startDate,
        LocalTime startTime,
        LocalDate endDate,
        LocalTime endTime,
        List<String> daysOfWeek)
        implements ScheduleDto {}
