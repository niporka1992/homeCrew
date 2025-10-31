package ru.homecrew.service.scheduler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.homecrew.dto.scheduler.type.*;
import ru.homecrew.service.scheduler.strategy.CronStrategy;

@DisplayName("ScheduleExpressionServiceImpl — выбор и генерация CRON по типу расписания")
class ScheduleExpressionServiceImplTest {

    @Mock
    private CronStrategy<DailyScheduleDto> dailyStrategy;

    @Mock
    private CronStrategy<WeeklyScheduleDto> weeklyStrategy;

    @Mock
    private CronStrategy<MonthlyScheduleDto> monthlyStrategy;

    @Mock
    private CronStrategy<YearlyScheduleDto> yearlyStrategy;

    @Mock
    private CronStrategy<CustomScheduleDto> customStrategy;

    private ScheduleExpressionServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        Map<String, CronStrategy<? extends ScheduleDto>> strategies = new HashMap<>();
        strategies.put("DAILY", dailyStrategy);
        strategies.put("WEEKLY", weeklyStrategy);
        strategies.put("MONTHLY", monthlyStrategy);
        strategies.put("YEARLY", yearlyStrategy);
        strategies.put("CUSTOM", customStrategy);

        service = new ScheduleExpressionServiceImpl(strategies);
    }

    @Test
    @DisplayName("toCron(): корректно вызывает DAILY стратегию")
    void toCron_usesDailyStrategy() {
        DailyScheduleDto dto = new DailyScheduleDto("job", LocalDate.now(), LocalTime.NOON, null, null);
        when(dailyStrategy.generate(dto)).thenReturn("0 0 12 * * ?");

        String result = service.toCron(dto);

        assertEquals("0 0 12 * * ?", result);
        verify(dailyStrategy).generate(dto);
    }

    @Test
    @DisplayName("toCron(): корректно вызывает WEEKLY стратегию")
    void toCron_usesWeeklyStrategy() {
        WeeklyScheduleDto dto = new WeeklyScheduleDto("job", null, LocalTime.of(10, 0), null, null, null);
        when(weeklyStrategy.generate(dto)).thenReturn("0 0 10 ? * MON");

        String result = service.toCron(dto);

        assertEquals("0 0 10 ? * MON", result);
        verify(weeklyStrategy).generate(dto);
    }

    @Test
    @DisplayName("toCron(): корректно вызывает MONTHLY стратегию")
    void toCron_usesMonthlyStrategy() {
        MonthlyScheduleDto dto = new MonthlyScheduleDto("job", null, LocalTime.of(9, 30), null, null, null);
        when(monthlyStrategy.generate(dto)).thenReturn("0 30 9 1 * ?");

        String result = service.toCron(dto);

        assertEquals("0 30 9 1 * ?", result);
        verify(monthlyStrategy).generate(dto);
    }

    @Test
    @DisplayName("toCron(): корректно вызывает YEARLY стратегию")
    void toCron_usesYearlyStrategy() {
        YearlyScheduleDto dto = new YearlyScheduleDto("job", LocalDate.of(2025, 1, 1), LocalTime.of(0, 0), null, null);
        when(yearlyStrategy.generate(dto)).thenReturn("0 0 0 1 1 ?");

        String result = service.toCron(dto);

        assertEquals("0 0 0 1 1 ?", result);
        verify(yearlyStrategy).generate(dto);
    }

    @Test
    @DisplayName("toCron(): корректно вызывает CUSTOM стратегию")
    void toCron_usesCustomStrategy() {
        CustomScheduleDto dto = new CustomScheduleDto("job", null, null, null, null, "0 0 6 * * ?");
        when(customStrategy.generate(dto)).thenReturn("0 0 6 * * ?");

        String result = service.toCron(dto);

        assertEquals("0 0 6 * * ?", result);
        verify(customStrategy).generate(dto);
    }

    @Test
    @DisplayName("toCron(): выбрасывает исключение, если DTO = null")
    void toCron_nullDto_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.toCron(null));
    }

    @Test
    @DisplayName("toCron(): выбрасывает исключение при неизвестном типе DTO")
    void toCron_unknownDtoType_throws() {
        ScheduleDto unknown = () -> "job";

        assertThrows(IllegalArgumentException.class, () -> service.toCron(unknown));
    }

    @Test
    @DisplayName("toCron(): выбрасывает исключение при отсутствии стратегии в Map")
    void toCron_missingStrategy_throws() {
        Map<String, CronStrategy<? extends ScheduleDto>> map = new HashMap<>();
        ScheduleExpressionServiceImpl localService = new ScheduleExpressionServiceImpl(map);
        DailyScheduleDto dto = new DailyScheduleDto("job", null, null, null, null);

        assertThrows(IllegalArgumentException.class, () -> localService.toCron(dto));
    }
}
