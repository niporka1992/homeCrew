package ru.homecrew.model.interaction;

/**
 * Элемент взаимодействия — действие, доступное пользователю.
 */
public record ActionOption(
        String label, // Отображаемый текст
        String actionCode // Код или данные, передаваемые при выборе
        ) {}
