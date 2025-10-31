package ru.homecrew.service.task;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.homecrew.dto.task.TaskCommentDto;
import ru.homecrew.entity.AppUser;
import ru.homecrew.entity.task.Task;
import ru.homecrew.entity.task.TaskComment;
import ru.homecrew.entity.task.TaskHistory;
import ru.homecrew.enums.TaskActionType;
import ru.homecrew.mapper.TaskCommentMapper;
import ru.homecrew.repository.task.TaskHistoryRepository;
import ru.homecrew.repository.task.TaskRepository;

@Service
@RequiredArgsConstructor
public class TaskCommentServiceImpl implements TaskCommentService {

    private final TaskRepository taskRepository;
    private final TaskHistoryRepository historyRepository;
    private final TaskCommentMapper mapper;

    @Override
    @Transactional
    public TaskCommentDto addComment(Long taskId, AppUser user, String text) {
        Task task = taskRepository
                .findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Задача не найдена: " + taskId));

        TaskHistory history = TaskHistory.builder()
                .task(task)
                .appUser(user)
                .actionType(TaskActionType.COMMENT_ADDED)
                .build();

        TaskComment comment =
                TaskComment.builder().history(history).author(user).text(text).build();

        history.setComments(List.of(comment));
        historyRepository.save(history);

        return mapper.toDto(comment);
    }
}
