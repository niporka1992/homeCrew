package ru.homecrew.repository.task;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import ru.homecrew.entity.task.Task;
import ru.homecrew.enums.TaskStatus;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @EntityGraph(attributePaths = {"assignee"})
    List<Task> findByStatus(TaskStatus status);

    @EntityGraph(attributePaths = {"assignee"})
    List<Task> findByAssignee_Id(Long userId);

    @EntityGraph(attributePaths = {"assignee"})
    List<Task> findByStatusAndAssignee_Id(TaskStatus status, Long userId);

    @Override
    @EntityGraph(
            attributePaths = {
                "assignee",
                "history",
                "history.appUser",
                "history.comments",
                "history.comments.author",
                "history.attachments"
            })
    @NonNull
    Optional<Task> findById(@NonNull Long id);
}
