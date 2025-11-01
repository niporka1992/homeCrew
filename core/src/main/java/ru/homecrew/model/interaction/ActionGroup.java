package ru.homecrew.model.interaction;

import java.util.List;

/**
 * Группа действий, расположенных в одной строке или контексте.
 */
public record ActionGroup(List<ActionOption> actions) {

    public static ActionGroup of(ActionOption... actions) {
        return new ActionGroup(List.of(actions));
    }
}
