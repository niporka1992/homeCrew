package ru.homecrew.repository.task;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.homecrew.entity.task.TaskAttachment;

public interface TaskAttachmentRepository extends JpaRepository<TaskAttachment, Long> {}
