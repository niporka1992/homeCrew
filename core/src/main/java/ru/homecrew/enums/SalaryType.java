package ru.homecrew.enums;

import lombok.Getter;

@Getter
public enum SalaryType {
    FIXED("Фиксированная ставка"),
    PER_TASK("Оплата за задачу"),
    PER_HOUR("Оплата за часы");

    private final String displayName;

    SalaryType(String displayName) {
        this.displayName = displayName;
    }
}
