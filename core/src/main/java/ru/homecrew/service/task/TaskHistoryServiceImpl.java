package ru.homecrew.service.task;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.homecrew.entity.AppUser;
import ru.homecrew.entity.task.Task;
import ru.homecrew.entity.task.TaskHistory;
import ru.homecrew.enums.TaskActionType;
import ru.homecrew.repository.task.TaskHistoryRepository;
import ru.homecrew.repository.task.TaskRepository;

@Service
@RequiredArgsConstructor
public class TaskHistoryServiceImpl implements TaskHistoryService {

    private final TaskHistoryRepository historyRepository;
    private final TaskRepository taskRepository;

    @Override
    @Transactional
    public TaskHistory addHistory(Long taskId, AppUser user, TaskActionType type) {
        Task task = taskRepository
                .findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Задача не найдена: " + taskId));

        TaskHistory history =
                TaskHistory.builder().task(task).appUser(user).actionType(type).build();

        return historyRepository.save(history);
    }
}
