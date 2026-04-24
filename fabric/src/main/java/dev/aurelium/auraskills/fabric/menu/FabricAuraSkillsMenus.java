package dev.aurelium.auraskills.fabric.menu;

import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Dispatch helpers for built-in menus. **Sprint 29+:** resolution goes through
 * {@link FabricMenuRegistry} (ids match {@link FabricMenuKeys} / Bukkit {@code MenuFileManager} names).
 */
public final class FabricAuraSkillsMenus {

    public enum Id {
        SKILLS(FabricMenuKeys.SKILLS),
        STATS(FabricMenuKeys.STATS);

        private final String menuId;

        Id(String menuId) {
            this.menuId = menuId;
        }

        public String menuId() {
            return menuId;
        }
    }

    private FabricAuraSkillsMenus() {
    }

    public static void open(String menuId, ServerPlayerEntity player, FabricAuraSkills plugin) {
        plugin.getMenuRegistry().open(menuId, player, plugin);
    }

    public static void open(Id id, ServerPlayerEntity player, FabricAuraSkills plugin) {
        open(id.menuId(), player, plugin);
    }

    /**
     * Bukkit id {@code level_progression} (see {@link FabricMenuKeys#LEVEL_PROGRESSION}): not registered in
     * {@link FabricMenuRegistry} (needs a {@link Skill}); opened programmatically from the Skills menu.
     */
    public static void openLevelProgression(Skill skill, ServerPlayerEntity player, FabricAuraSkills plugin) {
        FabricLevelProgressionMenuGui.open(player, plugin, skill);
    }
}
