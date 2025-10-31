package ru.homecrew.repository.task;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.homecrew.entity.task.TaskHistory;

public interface TaskHistoryRepository extends JpaRepository<TaskHistory, Long> {}
