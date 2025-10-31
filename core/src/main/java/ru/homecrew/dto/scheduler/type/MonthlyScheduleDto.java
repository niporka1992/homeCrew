package ru.homecrew.dto.scheduler.type;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@JsonTypeName("MONTHLY")
public record MonthlyScheduleDto(
        String jobName,
        LocalDate startDate,
        LocalTime startTime,
        LocalDate endDate,
        LocalTime endTime,
        List<Integer> daysOfMonth)
        implements ScheduleDto {}
