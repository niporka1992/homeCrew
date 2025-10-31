package ru.homecrew.enums;

import lombok.Getter;
import org.quartz.CronTrigger;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;

/**
 * {@code TaskTypeTrigger} — отражает тип триггера Quartz.
 * Привязан напрямую к классам Quartz для простого сопоставления.
 */
@Getter
public enum TaskTypeTrigger {
    SIMPLE(SimpleTrigger.class),
    CRON(CronTrigger.class);

    private final Class<? extends Trigger> quartzClass;

    TaskTypeTrigger(Class<? extends Trigger> quartzClass) {
        this.quartzClass = quartzClass;
    }

    /**
     * Определяет тип по самому триггеру Quartz.
     */
    public static TaskTypeTrigger from(Trigger trigger) {
        if (trigger == null) {
            throw new IllegalArgumentException("Trigger не может быть null");
        }

        for (TaskTypeTrigger type : values()) {
            if (type.quartzClass.isAssignableFrom(trigger.getClass())) {
                return type;
            }
        }

        throw new IllegalArgumentException(
                "Неизвестный тип триггера Quartz: " + trigger.getClass().getName());
    }
}
