package ru.homecrew.service.task;

import ru.homecrew.dto.task.TaskCommentDto;
import ru.homecrew.entity.AppUser;

public interface TaskCommentService {

    /**
     * Добавить комментарий к задаче
     */
    TaskCommentDto addComment(Long taskId, AppUser user, String text);
}
