package ru.homecrew.service.scheduler.strategy;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import ru.homecrew.dto.scheduler.type.MonthlyScheduleDto;

@SuppressWarnings("java:S6830")
@Component("MONTHLY")
public class MonthlyCronStrategy implements CronStrategy<MonthlyScheduleDto> {
    @Override
    public String generate(MonthlyScheduleDto dto) {
        LocalTime t = Optional.ofNullable(dto.startTime()).orElse(LocalTime.MIDNIGHT);
        List<Integer> days = Optional.ofNullable(dto.daysOfMonth())
                .filter(list -> !list.isEmpty())
                .orElse(List.of(1));
        String dayExpr = days.stream().map(String::valueOf).collect(Collectors.joining(","));
        return String.format("0 %d %d %s * ?", t.getMinute(), t.getHour(), dayExpr);
    }
}
