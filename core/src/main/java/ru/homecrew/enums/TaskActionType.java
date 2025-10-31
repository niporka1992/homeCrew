package ru.homecrew.enums;

import lombok.Getter;

@Getter
public enum TaskActionType {
    CREATED("Создана задача"),
    STATUS_CHANGED("Изменён статус"),
    COMMENT_ADDED("Добавлен комментарий"),
    ATTACHMENT_ADDED("Добавлено вложение");

    private final String description;

    TaskActionType(String description) {
        this.description = description;
    }
}
