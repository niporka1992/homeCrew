package ru.homecrew.service.scheduler.strategy;

import java.time.LocalTime;
import java.util.Optional;
import org.springframework.stereotype.Component;
import ru.homecrew.dto.scheduler.type.DailyScheduleDto;

@SuppressWarnings("java:S6830")
@Component("DAILY")
public class DailyCronStrategy implements CronStrategy<DailyScheduleDto> {
    @Override
    public String generate(DailyScheduleDto dto) {
        LocalTime t = Optional.ofNullable(dto.startTime()).orElse(LocalTime.MIDNIGHT);
        return String.format("0 %d %d * * ?", t.getMinute(), t.getHour());
    }
}
