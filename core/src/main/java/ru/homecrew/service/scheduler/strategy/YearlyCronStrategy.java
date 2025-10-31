package ru.homecrew.service.scheduler.strategy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.springframework.stereotype.Component;
import ru.homecrew.dto.scheduler.type.YearlyScheduleDto;

@SuppressWarnings("java:S6830")
@Component("YEARLY")
public class YearlyCronStrategy implements CronStrategy<YearlyScheduleDto> {
    @Override
    public String generate(YearlyScheduleDto dto) {
        LocalDate date = Optional.ofNullable(dto.startDate())
                .orElse(LocalDate.of(LocalDate.now().getYear(), 1, 1));
        LocalTime time = Optional.ofNullable(dto.startTime()).orElse(LocalTime.MIDNIGHT);
        return String.format(
                "0 %d %d %d %d ?", time.getMinute(), time.getHour(), date.getDayOfMonth(), date.getMonthValue());
    }
}
