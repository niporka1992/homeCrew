package ru.homecrew.service.bot.ui;

import java.util.List;

/**
 * Ряд кнопок (inline).
 */
public record UiKeyboardRow(List<UiButton> buttons) {

    public static UiKeyboardRow of(UiButton... buttons) {
        return new UiKeyboardRow(List.of(buttons));
    }
}
