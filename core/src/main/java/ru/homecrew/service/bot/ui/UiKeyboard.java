package ru.homecrew.service.bot.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Универсальная клавиатура для любого мессенджера.
 */
public class UiKeyboard {

    private final List<UiKeyboardRow> rows = new ArrayList<>();

    public UiKeyboard() {}

    public UiKeyboard(List<UiKeyboardRow> rows) {
        this.rows.addAll(rows);
    }

    public static UiKeyboard ofRows(UiKeyboardRow... rows) {
        return new UiKeyboard(Arrays.asList(rows));
    }

    public void addRow(UiButton... buttons) {
        rows.add(new UiKeyboardRow(Arrays.asList(buttons)));
    }

    public List<UiKeyboardRow> rows() {
        return rows;
    }

    @Override
    public String toString() {
        return "UiKeyboard{" + "rows=" + rows + '}';
    }
}
