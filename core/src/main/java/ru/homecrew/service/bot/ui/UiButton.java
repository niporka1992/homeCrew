package ru.homecrew.service.bot.ui;

/**
 * Универсальная кнопка интерфейса для ботов
 */
public record UiButton(
        String label, // Текст на кнопке
        String callbackData // Данные, передаваемые при нажатии
        ) {}
