package dev.aurelium.auraskills.fabric.menu;

import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.stat.Stat;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Optional context for {@link FabricMenuRegistry#open} when a built-in id needs a {@link Skill} or
 * {@link Stat} (Bukkit “menu context” / Slate placeholder — bounded subset, Sprint 35+). Static menus
 * use {@link #none()}; callers must not set both a skill and a stat on one value (factory methods
 * enforce this).
 */
public record FabricMenuContext(@Nullable Skill skill, @Nullable Stat stat) {

    public static FabricMenuContext none() {
        return new FabricMenuContext(null, null);
    }

    public static FabricMenuContext ofSkill(Skill skill) {
        return new FabricMenuContext(Objects.requireNonNull(skill, "skill"), null);
    }

    public static FabricMenuContext ofStat(Stat stat) {
        return new FabricMenuContext(null, Objects.requireNonNull(stat, "stat"));
    }
}
