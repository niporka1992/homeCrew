package ru.homecrew.service.task;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import ru.homecrew.entity.task.TaskAttachment;
import ru.homecrew.entity.task.TaskHistory;
import ru.homecrew.repository.task.TaskAttachmentRepository;
import ru.homecrew.repository.task.TaskHistoryRepository;

@DisplayName("TaskAttachmentServiceImpl — добавление вложений")
class TaskAttachmentServiceImplTest {

    @Mock
    private TaskAttachmentRepository attachmentRepository;

    @Mock
    private TaskHistoryRepository historyRepository;

    @InjectMocks
    private TaskAttachmentServiceImpl service;

    private TaskHistory history;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        history = TaskHistory.builder().build();
    }

    @Test
    @DisplayName("addAttachment(): успешно сохраняет вложение, если история найдена")
    void addAttachment_success() {
        when(historyRepository.findById(1L)).thenReturn(Optional.of(history));

        service.addAttachment(1L, "https://file.jpg");

        ArgumentCaptor<TaskAttachment> captor = ArgumentCaptor.forClass(TaskAttachment.class);
        verify(attachmentRepository).save(captor.capture());

        TaskAttachment saved = captor.getValue();
        assertEquals("https://file.jpg", saved.getFileUrl());
        assertSame(history, saved.getHistory());
    }

    @Test
    @DisplayName("addAttachment(): выбрасывает EntityNotFoundException, если история не найдена")
    void addAttachment_historyNotFound() {
        when(historyRepository.findById(42L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.addAttachment(42L, "https://file.jpg"));

        verify(attachmentRepository, never()).save(any());
    }
}
