package ru.homecrew.service.task;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.homecrew.entity.task.TaskAttachment;
import ru.homecrew.entity.task.TaskHistory;
import ru.homecrew.repository.task.TaskAttachmentRepository;
import ru.homecrew.repository.task.TaskHistoryRepository;

@Service
@RequiredArgsConstructor
public class TaskAttachmentServiceImpl implements TaskAttachmentService {

    private final TaskAttachmentRepository repository;
    private final TaskHistoryRepository historyRepository;

    @Override
    @Transactional
    public void addAttachment(Long historyId, String fileUrl) {
        TaskHistory history = historyRepository
                .findById(historyId)
                .orElseThrow(() -> new EntityNotFoundException("История задачи не найдена: " + historyId));

        TaskAttachment attachment =
                TaskAttachment.builder().history(history).fileUrl(fileUrl).build();

        repository.save(attachment);
    }
}
