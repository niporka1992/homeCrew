package ru.homecrew.service.notification;

import ru.homecrew.dto.task.TaskDto;

/**
 * Универсальный интерфейс для оповещений о задачах.
 * Реализация может быть через Telegram, Email и т.д.
 */
public interface TaskNotificationService {
    void notifyNewTask(TaskDto task);
}
