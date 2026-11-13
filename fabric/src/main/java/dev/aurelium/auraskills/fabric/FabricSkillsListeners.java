package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.source.XpSource;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.common.util.data.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Fabric listener bus for Granitelib and other mods. The mod id and packages stay {@code auraskills}.
 * Register during server start. Listeners run after the built-in anti-AFK check and may replace its result.
 */
public final class FabricSkillsListeners {

    private static final CopyOnWriteArrayList<Listener> LISTENERS = new CopyOnWriteArrayList<>();

    private FabricSkillsListeners() {
    }

    public static void register(Listener listener) {
        LISTENERS.addIfAbsent(listener);
    }

    public static void unregister(Listener listener) {
        LISTENERS.remove(listener);
    }

    public static List<Listener> listeners() {
        return List.copyOf(LISTENERS);
    }

    public interface Listener {

        default void onUserLoad(User user) {
        }

        default void onSkillLevelUp(User user, Skill skill, int level) {
        }

        /**
         * @return a replacement cancel flag and XP amount, or null to leave the current values
         */
        default @Nullable Pair<Boolean, Double> onXpGain(User user, Skill skill, @Nullable XpSource source, double amount) {
            return null;
        }
    }
}
