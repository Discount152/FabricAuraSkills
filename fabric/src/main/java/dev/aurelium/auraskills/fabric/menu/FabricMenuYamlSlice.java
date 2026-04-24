package dev.aurelium.auraskills.fabric.menu;

import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
/**
 * Granite slice of a built-in menu loaded from {@code menus/<id>.yml} under the {@code granite:} key.
 * Not a full Slate {@code skills.yml} / {@code stats.yml} document.
 */
public record FabricMenuYamlSlice(
        boolean enabled,
        String titleTranslationKey,
        int rows,
        int columns,
        Map<String, String> formatKeys) {

    public static final int EXPECTED_ROWS = 6;
    public static final int EXPECTED_COLS = 9;

    public FabricMenuYamlSlice {
        formatKeys = formatKeys == null
                ? Map.of()
                : Collections.unmodifiableMap(formatKeys);
    }

    static FabricMenuYamlSlice defaultSkills() {
        return new FabricMenuYamlSlice(
                true,
                "auraskills.granite.menu.skills_title",
                EXPECTED_ROWS,
                EXPECTED_COLS,
                Map.of("list_open_detail", "auraskills.granite.menu.skills_lore_open_detail"));
    }

    static FabricMenuYamlSlice defaultStats() {
        return new FabricMenuYamlSlice(
                true,
                "auraskills.granite.menu.stats_title",
                EXPECTED_ROWS,
                EXPECTED_COLS,
                Map.of("list_open_detail", "auraskills.granite.menu.stats_lore_open_detail"));
    }

    /** Detail menu: {@code menus/level_progression.yml} {@code granite.format.*} keys. */
    static FabricMenuYamlSlice defaultLevelProgression() {
        return new FabricMenuYamlSlice(
                true,
                "auraskills.granite.menu.level_progression_title",
                EXPECTED_ROWS,
                EXPECTED_COLS,
                Map.ofEntries(
                        Map.entry("main_title", "auraskills.granite.menu.level_progression_main_title"),
                        Map.entry("lore_level", "auraskills.granite.menu.level_progression_lore_level"),
                        Map.entry("lore_xp", "auraskills.granite.menu.level_progression_lore_xp"),
                        Map.entry("lore_to_next", "auraskills.granite.menu.level_progression_lore_to_next"),
                        Map.entry("lore_maxed", "auraskills.granite.menu.level_progression_lore_maxed"),
                        Map.entry("back", "auraskills.granite.menu.level_progression_back"),
                        Map.entry("back_lore", "auraskills.granite.menu.level_progression_back_lore"),
                        Map.entry("no_permission", "auraskills.granite.menu.skills_lore_no_permission")));
    }

    /** Detail menu: {@code menus/stat_info.yml} {@code granite.format.*} keys. */
    static FabricMenuYamlSlice defaultStatInfo() {
        return new FabricMenuYamlSlice(
                true,
                "auraskills.granite.menu.stat_info_title",
                EXPECTED_ROWS,
                EXPECTED_COLS,
                Map.ofEntries(
                        Map.entry("main_title", "auraskills.granite.menu.stat_info_main_title"),
                        Map.entry("lore_value", "auraskills.granite.menu.stat_info_lore_value"),
                        Map.entry("back", "auraskills.granite.menu.stat_info_back"),
                        Map.entry("back_lore", "auraskills.granite.menu.stat_info_back_lore")));
    }

    @Nullable
    public String getFormatKey(String name, @Nullable String codeFallback) {
        String v = formatKeys.get(name);
        if (v != null && !v.isEmpty()) {
            return v;
        }
        return codeFallback;
    }

    /**
     * Resolved key for this slice, then {@code codeDefaults}, then {@code hardFallback} (after-load merge
     * should make this rare).
     */
    public String resolveFormatKey(String name, FabricMenuYamlSlice codeDefaults, String hardFallback) {
        String v = getFormatKey(name, null);
        if (v != null) {
            return v;
        }
        return codeDefaults.getFormatKey(name, hardFallback);
    }
}
