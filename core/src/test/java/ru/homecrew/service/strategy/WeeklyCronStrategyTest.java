package ru.homecrew.service.strategy;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.homecrew.dto.scheduler.type.WeeklyScheduleDto;
import ru.homecrew.service.scheduler.strategy.WeeklyCronStrategy;

@DisplayName("WeeklyCronStrategy — генерация CRON для еженедельного расписания")
class WeeklyCronStrategyTest {

    private final WeeklyCronStrategy strategy = new WeeklyCronStrategy();

    @Test
    @DisplayName("generate(): корректно формирует CRON при указанных днях и времени")
    void generate_withDaysAndTime() {
        WeeklyScheduleDto dto =
                new WeeklyScheduleDto("job", LocalDate.now(), LocalTime.of(10, 15), null, null, List.of("MON", "FRI"));
        String result = strategy.generate(dto);
        assertEquals("0 15 10 ? * MON,FRI", result);
    }

    @Test
    @DisplayName("generate(): если дни недели не указаны — подставляет *")
    void generate_noDays_defaultsToAllDays() {
        WeeklyScheduleDto dto =
                new WeeklyScheduleDto("job", LocalDate.now(), LocalTime.of(6, 0), null, null, List.of());
        String result = strategy.generate(dto);
        assertEquals("0 0 6 ? * *", result);
    }

    @Test
    @DisplayName("generate(): если время не указано — берёт полночь")
    void generate_nullTime_defaultsToMidnight() {
        WeeklyScheduleDto dto = new WeeklyScheduleDto("job", LocalDate.now(), null, null, null, List.of("TUE"));
        String result = strategy.generate(dto);
        assertEquals("0 0 0 ? * TUE", result);
    }
}
