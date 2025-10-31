package ru.homecrew.dto.scheduler.type;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.time.LocalDate;
import java.time.LocalTime;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = DailyScheduleDto.class, name = "DAILY"),
    @JsonSubTypes.Type(value = WeeklyScheduleDto.class, name = "WEEKLY"),
    @JsonSubTypes.Type(value = MonthlyScheduleDto.class, name = "MONTHLY"),
    @JsonSubTypes.Type(value = YearlyScheduleDto.class, name = "YEARLY"),
    @JsonSubTypes.Type(value = CustomScheduleDto.class, name = "CUSTOM"),
    @JsonSubTypes.Type(value = SimpleScheduleDto.class, name = "SIMPLE") // если хочешь и simple сюда
})
public interface ScheduleDto {

    /**
     * Общие поля для всех расписаний.
     * Необязательно реализовывать их во всех типах (record сам их даёт).
     */
    String jobName();

    default LocalDate startDate() {
        return null;
    }

    default LocalTime startTime() {
        return null;
    }
}
