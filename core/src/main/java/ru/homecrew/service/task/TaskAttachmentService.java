package ru.homecrew.service.task;

public interface TaskAttachmentService {

    /**
     * Добавляет вложение (фото, видео, файл) к конкретной записи истории.
     *
     * @param historyId ID записи истории задачи
     * @param fileUrl   URL или fileId файла
     * @return сохранённый объект TaskAttachment
     */
    void addAttachment(Long historyId, String fileUrl);
}
