package ru.homecrew.service.task;

import ru.homecrew.entity.AppUser;
import ru.homecrew.entity.task.TaskHistory;
import ru.homecrew.enums.TaskActionType;

public interface TaskHistoryService {

    /**
     * Добавляет запись истории с типом действия.
     */
    TaskHistory addHistory(Long taskId, AppUser user, TaskActionType type);
}
