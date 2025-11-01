package ru.homecrew.model.interaction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Модель расположения действий — абстрактный "интерфейс выбора",
 * который может быть преобразован под любой клиент (бот, web, cli).
 */
public class ActionLayout {

    private final List<ActionGroup> groups = new ArrayList<>();

    public ActionLayout() {}

    public ActionLayout(List<ActionGroup> groups) {
        this.groups.addAll(groups);
    }

    public static ActionLayout ofGroups(ActionGroup... groups) {
        return new ActionLayout(Arrays.asList(groups));
    }

    public void addGroup(ActionOption... actions) {
        groups.add(new ActionGroup(Arrays.asList(actions)));
    }

    public List<ActionGroup> groups() {
        return groups;
    }

    @Override
    public String toString() {
        return "ActionLayout{" + "groups=" + groups + '}';
    }
}
