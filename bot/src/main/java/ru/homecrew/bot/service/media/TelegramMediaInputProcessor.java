package ru.homecrew.bot.service.media;

import java.util.*;
import java.util.concurrent.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import ru.homecrew.bot.service.UserInputWaiter;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramMediaInputProcessor implements MediaInputProcessor {

    private final UserInputWaiter inputWaiter;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final Map<String, List<String>> mediaGroupBuffer = new ConcurrentHashMap<>();

    @Override
    public boolean process(Object rawEvent) {
        if (!(rawEvent instanceof Update update)) {
            return false;
        }

        try {
            Long chatId = extractChatId(update);
            if (chatId == null || !update.hasMessage()) {
                return false;
            }

            Message msg = update.getMessage();
            List<String> fileIds = extractFileIds(msg);

            // Медиа-группа
            if (msg.getMediaGroupId() != null) {
                String groupId = msg.getMediaGroupId();
                mediaGroupBuffer
                        .computeIfAbsent(groupId, k -> Collections.synchronizedList(new ArrayList<>()))
                        .addAll(fileIds);

                flushLater(chatId, groupId);
                return true;
            }

            // Одиночные файлы
            if (!fileIds.isEmpty() && inputWaiter.processMedia(chatId, fileIds)) {
                log.debug(" Обработано {} файл(ов) от chatId={}", fileIds.size(), chatId);
                return true;
            }

            // Текст
            String text = extractText(update);
            if (text != null && inputWaiter.processInput(chatId, text)) {
                log.debug(" Обработан ожидаемый текстовый ввод от chatId={}", chatId);
                return true;
            }

        } catch (Exception e) {
            log.warn(" Ошибка в TelegramMediaInputProcessor: {}", e.getMessage(), e);
        }
        return false;
    }

    private List<String> extractFileIds(Message msg) {
        List<String> fileIds = new ArrayList<>();

        if (msg.hasPhoto()) {
            var photos = msg.getPhoto();
            if (photos != null && !photos.isEmpty()) {
                fileIds.add(photos.getLast().getFileId());
            }
        }

        if (msg.hasVideo()) {
            fileIds.add(msg.getVideo().getFileId());
        }

        if (msg.hasDocument()) {
            fileIds.add(msg.getDocument().getFileId());
        }

        return fileIds;
    }

    private void flushLater(Long chatId, String groupId) {
        scheduler.schedule(
                () -> {
                    var files = mediaGroupBuffer.remove(groupId);
                    if (files != null && !files.isEmpty()) {
                        inputWaiter.processMedia(chatId, files);
                        log.info("📦 MediaGroup {} обработана: {} файлов", groupId, files.size());
                    }
                },
                1500,
                TimeUnit.MILLISECONDS);
    }

    private Long extractChatId(Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getChatId();
        }
        if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getFrom().getId();
        }
        return null;
    }

    private String extractText(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            return update.getMessage().getText();
        }
        if (update.hasEditedMessage() && update.getEditedMessage().hasText()) {
            return update.getEditedMessage().getText();
        }
        return null;
    }
}
