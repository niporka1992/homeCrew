package ru.homecrew.service.scheduler.strategy;

import java.time.LocalTime;
import java.util.Optional;
import org.springframework.stereotype.Component;
import ru.homecrew.dto.scheduler.type.WeeklyScheduleDto;

@SuppressWarnings("java:S6830")
@Component("WEEKLY")
public class WeeklyCronStrategy implements CronStrategy<WeeklyScheduleDto> {
    @Override
    public String generate(WeeklyScheduleDto dto) {
        LocalTime t = Optional.ofNullable(dto.startTime()).orElse(LocalTime.MIDNIGHT);
        String days = dto.daysOfWeek() == null || dto.daysOfWeek().isEmpty() ? "*" : String.join(",", dto.daysOfWeek());
        return String.format("0 %d %d ? * %s", t.getMinute(), t.getHour(), days);
    }
}
