package ru.homecrew.service.file;

import org.springframework.http.ResponseEntity;

/**
 * Контракт для получения файлов из различных источников.
 * Может быть реализован для Telegram, локального хранилища и т.п.
 */
public interface FileService {

    /**
     * Получить файл по идентификатору.
     * Например, для Telegram — это file_id, для локального — имя файла.
     *
     * @param fileId идентификатор или путь
     * @return ResponseEntity с потоком файла
     */
    ResponseEntity<byte[]> getFileById(String fileId);
}
