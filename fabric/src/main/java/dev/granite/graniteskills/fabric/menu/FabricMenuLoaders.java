package dev.granite.graniteskills.fabric.menu;

import dev.granite.graniteskills.fabric.FabricGraniteSkills;

/**
 * Binds default menu {@link FabricMenuDefinition definitions} to the {@link FabricMenuRegistry}. **Sprint
 * 32+:** per-menu granite slices load from {@code menus/*.yml} via
 * {@link FabricBuiltInMenuConfig} (reload with {@link dev.granite.graniteskills.fabric.FabricGraniteSkills#reloadFromDisk()}).
 * **Sprint 35:** list menus + context ids {@code level_progression}, {@code stat_info}.
 */
public final class FabricMenuLoaders {

    private FabricMenuLoaders() {
    }

    /**
     * Registers built-in slice menus: {@code skills}, {@code stats}, {@code level_progression},
     * {@code stat_info}. The {@code sources} view is opened from {@code /sources} and
     * {@code /openmenu sources} (**Sprint 69–70**) via
     * {@link FabricGraniteSkillsMenus#openSources} — not registered here. Call once during
     * {@link FabricGraniteSkills#enable()}.
     */
    public static void registerBuiltInMenus(FabricMenuRegistry registry, FabricGraniteSkills plugin) {
        registry.register(new FabricMenuDefinition(
                FabricMenuKeys.SKILLS,
                "graniteskills.granite.menu.skills_title",
                FabricSkillsMenuGui::open));
        registry.register(new FabricMenuDefinition(
                FabricMenuKeys.STATS,
                "graniteskills.granite.menu.stats_title",
                FabricStatsMenuGui::open));
        registry.register(new FabricMenuDefinition(
                FabricMenuKeys.LEVEL_PROGRESSION,
                "graniteskills.granite.menu.level_progression_title",
                FabricLevelProgressionMenuGui::open));
        registry.register(new FabricMenuDefinition(
                FabricMenuKeys.STAT_INFO,
                "graniteskills.granite.menu.stat_info_title",
                FabricStatInfoMenuGui::open));
        int n = registry.menuIds().size();
        plugin.logger()
                .info("Registered " + n + " built-in Fabric menu id(s): " + String.join(", ", registry.menuIds()));
    }
}
