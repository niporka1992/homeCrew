package ru.homecrew.service;

import ru.homecrew.service.bot.ui.UiKeyboard;

/**
 * Универсальный интерфейс для отправки сообщений в мессенджеры.
 */
public interface BotMessenger {

    /** Отправка простого текста */
    void sendMessage(Long chatId, String text);

    /** Отправка сообщения с inline-кнопками */
    void sendMessageWithKeyboard(Long chatId, String text, UiKeyboard keyboard);

    /** Экранирование MarkdownV2 символов */
    default String escapeMarkdown(String text) {
        if (text == null) return "";
        return text.replace("_", "\\_")
                .replace("*", "\\*")
                .replace("[", "\\[")
                .replace("]", "\\]")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("~", "\\~")
                .replace("`", "\\`")
                .replace(">", "\\>")
                .replace("#", "\\#")
                .replace("+", "\\+")
                .replace("-", "\\-")
                .replace("=", "\\=")
                .replace("|", "\\|")
                .replace("{", "\\{")
                .replace("}", "\\}")
                .replace(".", "\\.")
                .replace("!", "\\!");
    }
}
