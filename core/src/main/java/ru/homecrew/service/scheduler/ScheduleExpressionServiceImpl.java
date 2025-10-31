package ru.homecrew.service.scheduler;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.homecrew.dto.scheduler.type.*;
import ru.homecrew.service.scheduler.strategy.CronStrategy;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleExpressionServiceImpl implements ScheduleExpressionService {

    private final Map<String, CronStrategy<? extends ScheduleDto>> strategies;

    @Override
    public String toCron(ScheduleDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("ScheduleDto не может быть null");
        }

        String key = resolveStrategyKey(dto);

        CronStrategy<ScheduleDto> strategy = getStrategy(key);
        String cron = strategy.generate(dto);

        log.debug("Сгенерировано CRON [{}]: {}", key, cron);
        return cron;
    }

    private String resolveStrategyKey(ScheduleDto dto) {
        return switch (dto) {
            case DailyScheduleDto ignored -> "DAILY";
            case WeeklyScheduleDto ignored -> "WEEKLY";
            case MonthlyScheduleDto ignored -> "MONTHLY";
            case YearlyScheduleDto ignored -> "YEARLY";
            case CustomScheduleDto ignored -> "CUSTOM";
            default ->
                throw new IllegalArgumentException(
                        "Неизвестный тип DTO: " + dto.getClass().getSimpleName());
        };
    }

    @SuppressWarnings("unchecked")
    private CronStrategy<ScheduleDto> getStrategy(String key) {
        CronStrategy<?> strategy = strategies.get(key);
        if (strategy == null) {
            throw new IllegalArgumentException("Неизвестная стратегия расписания: " + key);
        }
        return (CronStrategy<ScheduleDto>) strategy;
    }
}
