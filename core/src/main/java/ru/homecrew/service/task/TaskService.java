package ru.homecrew.service.task;

import java.util.List;
import ru.homecrew.dto.task.TaskCreateDto;
import ru.homecrew.dto.task.TaskDetailsDto;
import ru.homecrew.dto.task.TaskDto;
import ru.homecrew.entity.AppUser;
import ru.homecrew.enums.TaskStatus;

/**
 * Сервис управления задачами.
 * Обеспечивает создание, получение, обновление, удаление и изменение состояния задач.
 * Отвечает за связь между пользователями и задачами, а также за хранение истории изменений.
 */
public interface TaskService {

    /** Создаёт новую задачу. */
    TaskDto create(TaskCreateDto dto);

    /** Возвращает задачу по идентификатору. */
    TaskDto getById(Long id);

    /** Обновляет задачу. */
    TaskDto update(Long id, TaskCreateDto dto);

    /** Удаляет задачу по идентификатору. */
    void delete(Long id);

    /**
     * Меняет статус задачи (например: NEW → IN_PROGRESS, DONE и т.п.)
     * и автоматически добавляет запись в историю изменений.
     */
    void changeStatusAndAssign(Long id, TaskStatus newStatus, AppUser user);

    /**
     * Возвращает список задач, назначенных пользователю по его Telegram chatId.
     */
    List<TaskDto> getByAssignee(Long telegramChatId);

    /**
     * Возвращает задачи по статусу и пользователю (фильтр).
     */
    List<TaskDto> getTasks(String status, Long userId);

    /**
     * 🔥 Возвращает детальную информацию по задаче:
     * описание, историю изменений, комментарии и вложения.
     */
    TaskDetailsDto getTaskDetails(Long id);
}
