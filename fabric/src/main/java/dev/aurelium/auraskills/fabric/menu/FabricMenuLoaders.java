package dev.aurelium.auraskills.fabric.menu;

import dev.aurelium.auraskills.fabric.FabricAuraSkills;

/**
 * Binds default menu {@link FabricMenuDefinition definitions} to the {@link FabricMenuRegistry}. This
 * is the single "load" path for in-repo built-in menus (Sprint 29+); it does <strong>not</strong> read
 * YAML yet (see {@code fabric/MENU_AND_COMMANDS_PORT.md}).
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
