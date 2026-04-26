package dev.aurelium.auraskills.fabric.menu;

import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.stat.Stat;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Optional context for {@link FabricMenuRegistry#open} when a built-in id needs a {@link Skill} or
 * {@link Stat} (Bukkit “menu context” / Slate placeholder — bounded subset, Sprint 35+). Static menus
 * use {@link #none()}; callers must not set both a skill and a stat on one value (factory methods
 * enforce this). **Sprint 37:** {@code listPage} is for paged list menus (Skills, Stats) — 0-based index.
 * **Sprint 38:** {@link #ofSkill(Skill)}/{@link #ofStat(Stat)} with {@link #withListPage(int)} carries the
 * list page to restore when a detail menu (<strong>Level Progression</strong> / <strong>Stat Info</strong>)
 * uses <strong>Back</strong> to the parent list. **Sprint 100:** {@code progressionPage} is the 0-based page
 * index for template-backed <strong>level progression</strong> level grids (unused by other menus).
 */
public record FabricMenuContext(
        @Nullable Skill skill, @Nullable Stat stat, int listPage, int progressionPage) {

    public FabricMenuContext {
        if (listPage < 0) {
            listPage = 0;
        }
        if (progressionPage < 0) {
            progressionPage = 0;
        }
    }

    public static FabricMenuContext none() {
        return new FabricMenuContext(null, null, 0, 0);
    }

    public static FabricMenuContext ofSkill(Skill skill) {
        return new FabricMenuContext(Objects.requireNonNull(skill, "skill"), null, 0, 0);
    }

    public static FabricMenuContext ofStat(Stat stat) {
        return new FabricMenuContext(null, Objects.requireNonNull(stat, "stat"), 0, 0);
    }

    /** Copy with a new list page (Skills / Stats paged list open). */
    public FabricMenuContext withListPage(int page) {
        return new FabricMenuContext(skill, stat, page, progressionPage);
    }

    /** Copy with a new level-progression grid page (template path). */
    public FabricMenuContext withProgressionPage(int page) {
        return new FabricMenuContext(skill, stat, listPage, page);
    }
}
