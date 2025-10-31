package ru.homecrew.dto.scheduler.type;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.time.LocalDate;
import java.time.LocalTime;

@JsonTypeName("YEARLY")
public record YearlyScheduleDto(
        String jobName, LocalDate startDate, LocalTime startTime, LocalDate endDate, LocalTime endTime)
        implements ScheduleDto {}
