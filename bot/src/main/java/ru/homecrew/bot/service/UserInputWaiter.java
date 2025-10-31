package ru.homecrew.bot.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import org.springframework.stereotype.Component;

/**
 * UserInputWaiter — менеджер "ожидаемых" сообщений от пользователей.
 * Позволяет временно подвесить контекст и ждать следующего текстового ответа.
 */
@Component
public class UserInputWaiter {

    private final Map<Long, Consumer<String>> pendingInputs = new ConcurrentHashMap<>();
    private final Map<Long, Consumer<List<String>>> mediaInputs = new ConcurrentHashMap<>();

    /**
     * Регистрирует ожидание ввода от конкретного пользователя.
     */
    public void waitForInput(Long chatId, Consumer<String> action) {
        pendingInputs.put(chatId, action);
    }

    /**
     * Обрабатывает входящий текст.
     * Возвращает true, если сообщение было ожидаемым (и обработано callback’ом).
     */
    public boolean processInput(Long chatId, String text) {
        Consumer<String> action = pendingInputs.remove(chatId);
        if (action != null) {
            action.accept(text);
            return true;
        }
        return false;
    }

    public void waitForMediaInput(Long chatId, Consumer<List<String>> handler) {
        mediaInputs.put(chatId, handler);
    }

    public boolean processMedia(Long chatId, List<String> fileIds) {
        Consumer<List<String>> handler = mediaInputs.remove(chatId);
        if (handler != null) {
            handler.accept(fileIds);
            return true;
        }
        return false;
    }
}
