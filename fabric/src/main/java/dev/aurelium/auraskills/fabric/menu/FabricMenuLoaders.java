package dev.aurelium.auraskills.fabric.menu;

import dev.aurelium.auraskills.fabric.FabricAuraSkills;

/**
 * Binds default menu {@link FabricMenuDefinition definitions} to the {@link FabricMenuRegistry}. **Sprint
 * 32+:** per-menu granite slices load from {@code menus/skills.yml} and {@code menus/stats.yml} via
 * {@link FabricBuiltInMenuConfig} (reload with {@link dev.aurelium.auraskills.fabric.FabricAuraSkills#reloadFromDisk()}).
 */
public final class FabricMenuLoaders {

    private FabricMenuLoaders() {
    }

    /**
     * Registers Skills and Stats slice menus. Call once during {@link FabricAuraSkills#enable()}.
     */
    public static void registerBuiltInMenus(FabricMenuRegistry registry, FabricAuraSkills plugin) {
        registry.register(new FabricMenuDefinition(
                FabricMenuKeys.SKILLS,
                "auraskills.granite.menu.skills_title",
                FabricSkillsMenuGui::open));
        registry.register(new FabricMenuDefinition(
                FabricMenuKeys.STATS,
                "auraskills.granite.menu.stats_title",
                FabricStatsMenuGui::open));
        int n = registry.menuIds().size();
        plugin.logger().info("Registered " + n + " built-in Fabric menu id(s): " + String.join(", ", registry.menuIds()));
    }
}
