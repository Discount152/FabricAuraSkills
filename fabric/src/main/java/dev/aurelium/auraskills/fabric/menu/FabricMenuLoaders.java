package dev.aurelium.auraskills.fabric.menu;

import dev.aurelium.auraskills.fabric.FabricAuraSkills;

/**
 * Binds default menu {@link FabricMenuDefinition definitions} to the {@link FabricMenuRegistry}. **Sprint
 * 32+:** per-menu granite slices load from {@code menus/*.yml} via
 * {@link FabricBuiltInMenuConfig} (reload with {@link dev.aurelium.auraskills.fabric.FabricAuraSkills#reloadFromDisk()}).
 * **Sprint 35:** list menus + context ids {@code level_progression}, {@code stat_info}.
 */
public final class FabricMenuLoaders {

    private FabricMenuLoaders() {
    }

    /**
     * Registers built-in slice menus: {@code skills}, {@code stats}, {@code level_progression},
     * {@code stat_info}. Call once during {@link FabricAuraSkills#enable()}.
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
        registry.register(new FabricMenuDefinition(
                FabricMenuKeys.LEVEL_PROGRESSION,
                "auraskills.granite.menu.level_progression_title",
                FabricLevelProgressionMenuGui::open));
        registry.register(new FabricMenuDefinition(
                FabricMenuKeys.STAT_INFO,
                "auraskills.granite.menu.stat_info_title",
                FabricStatInfoMenuGui::open));
        int n = registry.menuIds().size();
        plugin.logger()
                .info("Registered " + n + " built-in Fabric menu id(s): " + String.join(", ", registry.menuIds()));
    }
}
