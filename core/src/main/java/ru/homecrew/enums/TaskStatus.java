package ru.homecrew.enums;

import lombok.Getter;

@Getter
public enum TaskStatus {
    NEW("Новая"),
    IN_PROGRESS("В процессе"),
    DONE("Выполнена"),
    EXPIRED("Просрочена");

    private final String displayName;

    TaskStatus(String displayName) {
        this.displayName = displayName;
    }
}
