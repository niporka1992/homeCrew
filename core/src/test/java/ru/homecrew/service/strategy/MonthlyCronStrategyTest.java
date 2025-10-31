package ru.homecrew.service.strategy;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.homecrew.dto.scheduler.type.MonthlyScheduleDto;
import ru.homecrew.service.scheduler.strategy.MonthlyCronStrategy;

@DisplayName("MonthlyCronStrategy — генерация CRON для ежемесячного расписания")
class MonthlyCronStrategyTest {

    private final MonthlyCronStrategy strategy = new MonthlyCronStrategy();

    @Test
    @DisplayName("generate(): корректно формирует CRON при заданных днях и времени")
    void generate_withDaysAndTime() {
        MonthlyScheduleDto dto =
                new MonthlyScheduleDto("job", LocalDate.now(), LocalTime.of(7, 45), null, null, List.of(5, 15));
        String result = strategy.generate(dto);
        assertEquals("0 45 7 5,15 * ?", result);
    }

    @Test
    @DisplayName("generate(): если дни месяца не указаны — берёт 1 число")
    void generate_noDays_defaultsToFirst() {
        MonthlyScheduleDto dto =
                new MonthlyScheduleDto("job", LocalDate.now(), LocalTime.of(12, 0), null, null, List.of());
        String result = strategy.generate(dto);
        assertEquals("0 0 12 1 * ?", result);
    }

    @Test
    @DisplayName("generate(): если время не указано — берёт полночь")
    void generate_nullTime_defaultsToMidnight() {
        MonthlyScheduleDto dto = new MonthlyScheduleDto("job", LocalDate.now(), null, null, null, List.of(10));
        String result = strategy.generate(dto);
        assertEquals("0 0 0 10 * ?", result);
    }
}
