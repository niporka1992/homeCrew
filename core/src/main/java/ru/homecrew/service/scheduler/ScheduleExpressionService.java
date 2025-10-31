package ru.homecrew.service.scheduler;

import ru.homecrew.dto.scheduler.type.ScheduleDto;

/**
 * Сервис преобразования пользовательских расписаний в cron-выражения.
 * Используется планировщиком для унификации форматов расписаний.
 */
public interface ScheduleExpressionService {

    /**
     * Конвертирует DTO-описание расписания в строку cron-формата,
     * понятную системам планирования - Quartz).
     *
     * @param dto объект с параметрами расписания (интервалы, дни, время и т.д.)
     * @return строка cron-выражения, готовая к передаче планировщику
     */
    String toCron(ScheduleDto dto);
}
