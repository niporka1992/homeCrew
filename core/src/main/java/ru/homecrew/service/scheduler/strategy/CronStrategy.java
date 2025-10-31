package ru.homecrew.service.scheduler.strategy;

import ru.homecrew.dto.scheduler.type.ScheduleDto;

public interface CronStrategy<T extends ScheduleDto> {
    String generate(T dto);
}
