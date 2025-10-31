package ru.homecrew.bot.util;

import lombok.experimental.UtilityClass;

/**
 * Централизованная фабрика для формирования callback-данных Telegram-бота.
 * Все ключи и паттерны определяются здесь.
 */
@UtilityClass
public class CallbackFactory {

    public static final String PREFIX_TASKS = "worker:tasks";
    public static final String PREFIX_TASKS_TAKE = "worker:tasks:take:";
    public static final String PREFIX_TASK = "worker:task:";
    public static final String PREFIX_TASK_DONE = "worker:task:done:";
    public static final String PREFIX_TASK_COMMENT = "worker:task:comment:";
    public static final String PREFIX_TASK_MEDIA = "worker:task:media:";

    public static String takeTask(long taskId) {
        return PREFIX_TASKS_TAKE + taskId;
    }
}
