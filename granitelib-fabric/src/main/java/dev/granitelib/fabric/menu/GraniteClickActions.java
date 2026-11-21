package dev.granitelib.fabric.menu;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * Dispatches a menu click to an action only when the actor is allowed to run it. The action
 * receives the actor. A denied actor does not run the action.
 */
public final class GraniteClickActions {

    private GraniteClickActions() {
    }

    public record Actor(UUID id, String name) {
        public Actor {
            if (id == null) {
                throw new IllegalArgumentException("actor id is required");
            }
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("actor name is required");
            }
        }
    }

    public record Click(String action, Actor actor, int slot) {
    }

    @FunctionalInterface
    public interface Permission {
        boolean allow(Actor actor, String action);
    }

    public static boolean dispatch(Actor actor, String action, int slot, Permission permission, Consumer<Click> actionRunner) {
        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException("action is required");
        }
        if (permission == null || actionRunner == null) {
            throw new IllegalArgumentException("permission and action are required");
        }
        if (!permission.allow(actor, action)) {
            return false;
        }
        actionRunner.accept(new Click(action, actor, slot));
        return true;
    }
}
