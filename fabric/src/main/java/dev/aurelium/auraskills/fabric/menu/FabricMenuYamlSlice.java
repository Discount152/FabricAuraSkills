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

    @Nullable
    public String getFormatKey(String name, @Nullable String codeFallback) {
        String v = formatKeys.get(name);
        if (v != null && !v.isEmpty()) {
            return v;
        }
        return codeFallback;
    }
}
