package ru.homecrew.bot.service.file;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.homecrew.service.file.FileService;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramFileService implements FileService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ApplicationContext context;

    @Value("${app.telegram.token}")
    private String botToken;

    private static final String TELEGRAM_API_BASE = "https://api.telegram.org";
    private static final String BOT_FILE_ENDPOINT = TELEGRAM_API_BASE + "/file/bot";
    private static final String BOT_API_ENDPOINT = TELEGRAM_API_BASE + "/bot";
    private static final String FILE_NOT_FOUND_MSG = "Файл не найден в Telegram";
    private static final String INTERNAL_ERROR_MSG = "Ошибка загрузки файла";

    @Override
    public ResponseEntity<byte[]> getFileById(String fileId) {
        try {
            TelegramFileService self = context.getBean(TelegramFileService.class);
            String filePath = self.getFilePathCached(fileId);

            if (filePath == null) {
                return ResponseEntity.status(404)
                        .contentType(MediaType.TEXT_PLAIN)
                        .body(FILE_NOT_FOUND_MSG.getBytes(StandardCharsets.UTF_8));
            }

            String fileUrl = String.format("%s%s/%s", BOT_FILE_ENDPOINT, botToken, filePath);
            HttpURLConnection conn =
                    (HttpURLConnection) URI.create(fileUrl).toURL().openConnection();
            conn.setRequestMethod("GET");

            String contentType =
                    Optional.ofNullable(conn.getContentType()).orElse(MediaType.APPLICATION_OCTET_STREAM_VALUE);

            try (InputStream in = conn.getInputStream()) {
                byte[] fileBytes = in.readAllBytes();
                String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);

                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                        .contentType(MediaType.parseMediaType(contentType))
                        .body(fileBytes);
            }

        } catch (Exception e) {
            log.error("Ошибка при получении файла из Telegram: {}", fileId, e);
            return ResponseEntity.internalServerError()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(INTERNAL_ERROR_MSG.getBytes(StandardCharsets.UTF_8));
        }
    }

    @Cacheable(value = "telegramFiles", key = "#fileId")
    public String getFilePathCached(String fileId) {
        try {
            String metaUrl = String.format("%s%s/getFile?file_id=%s", BOT_API_ENDPOINT, botToken, fileId);

            ResponseEntity<Map<String, Object>> responseEntity =
                    restTemplate.exchange(metaUrl, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});

            Map<String, Object> response = responseEntity.getBody();
            if (response == null || !Boolean.TRUE.equals(response.get("ok"))) {
                log.warn("Telegram API вернул ошибку для fileId={}", fileId);
                return null;
            }

            Object resultObj = response.get("result");
            if (!(resultObj instanceof Map<?, ?> resultMap)) {
                log.warn("Неверный формат ответа Telegram API для fileId={}", fileId);
                return null;
            }

            Object pathObj = resultMap.get("file_path");
            if (!(pathObj instanceof String filePath)) {
                log.warn("Отсутствует file_path в ответе Telegram API для fileId={}", fileId);
                return null;
            }

            log.debug("Кэшируем file_path для fileId={} → {}", fileId, filePath);
            return filePath;

        } catch (Exception e) {
            log.error("Ошибка при получении file_path из Telegram: {}", fileId, e);
            return null;
        }
    }
}
