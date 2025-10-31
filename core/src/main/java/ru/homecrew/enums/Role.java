package ru.homecrew.enums;

import lombok.Getter;

@Getter
public enum Role {
    OWNER("Владелец дома"),
    WORKER("Сотрудник"),
    GUEST("Новый юзер");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }
}
