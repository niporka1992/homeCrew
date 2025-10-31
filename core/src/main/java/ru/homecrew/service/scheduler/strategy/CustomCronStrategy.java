package ru.homecrew.service.scheduler.strategy;

import org.springframework.stereotype.Component;
import ru.homecrew.dto.scheduler.type.CustomScheduleDto;

@SuppressWarnings("java:S6830")
@Component("CUSTOM")
public class CustomCronStrategy implements CronStrategy<CustomScheduleDto> {
    @Override
    public String generate(CustomScheduleDto dto) {
        if (dto.customCron() == null || dto.customCron().isBlank()) {
            throw new IllegalArgumentException("customCron не может быть пустым");
        }
        return dto.customCron();
    }
}
