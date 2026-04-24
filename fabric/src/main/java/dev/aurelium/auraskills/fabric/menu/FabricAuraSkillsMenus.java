package dev.aurelium.auraskills.fabric.menu;

import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Dispatch helpers for built-in menus. **Sprint 29+:** resolution goes through
 * {@link FabricMenuRegistry} (ids match {@link FabricMenuKeys} / Bukkit {@code MenuFileManager} names).
 * **Sprint 35:** contextual ids {@code level_progression} and {@code stat_info} use
 * {@link FabricMenuContext} — prefer {@link #open(String, ServerPlayerEntity, FabricAuraSkills, FabricMenuContext)}
 * or the typed helpers.
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
        open(menuId, player, plugin, FabricMenuContext.none());
    }

    public static void open(
            String menuId,
            ServerPlayerEntity player,
            FabricAuraSkills plugin,
            FabricMenuContext context) {
        plugin.getMenuRegistry().open(menuId, player, plugin, context);
    }

    public static void open(Id id, ServerPlayerEntity player, FabricAuraSkills plugin) {
        open(id.menuId(), player, plugin);
    }

    /**
     * Bukkit id {@code level_progression} (see {@link FabricMenuKeys#LEVEL_PROGRESSION}): {@link Skill}
     * context is required; routes through {@link FabricMenuRegistry}.
     */
    public static void openLevelProgression(Skill skill, ServerPlayerEntity player, FabricAuraSkills plugin) {
        open(FabricMenuKeys.LEVEL_PROGRESSION, player, plugin, FabricMenuContext.ofSkill(skill));
    }

    /**
     * Bukkit id {@code stat_info} (see {@link FabricMenuKeys#STAT_INFO}): {@link Stat} context is required;
     * still gated in {@code FabricStatInfoMenuGui} by {@code menus.stat_info.enabled}. Routes through
     * {@link FabricMenuRegistry}.
     */
    public static void openStatInfo(Stat stat, ServerPlayerEntity player, FabricAuraSkills plugin) {
        open(FabricMenuKeys.STAT_INFO, player, plugin, FabricMenuContext.ofStat(stat));
    }
}
