package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.common.menu.MenuHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * Menu format strings for common code paths (e.g. {@link dev.aurelium.auraskills.common.reward.type.StatReward})
 * until a full YAML/Slate load matches Bukkit. Keys mirror {@code bukkit/src/main/resources/menus/*.yml}.
 */
public final class FabricMenuHelper implements MenuHelper {

    private final Map<String, Map<String, String>> formats = new HashMap<>();

    public FabricMenuHelper() {
        var level = new HashMap<String, String>();
        // Bukkit: menus/level_progression.yml
        level.put("stat_reward_entry", "\n  {color}+{num} {symbol} {stat}");
        formats.put("level_progression", level);
        var stats = new HashMap<String, String>();
        // Bukkit: menus/stats.yml
        stats.put("player_stat_entry", "{color}{symbol} {stat} <white>{level}");
        stats.put("trait_entry", "<#d0d0d0>{trait}: {color}{level}");
        formats.put("stats", stats);
    }

    @Override
    public String getFormat(String menuName, String formatName) {
        Map<String, String> m = formats.get(menuName);
        if (m == null) {
            return formatName;
        }
        return m.getOrDefault(formatName, formatName);
    }
}
