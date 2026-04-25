package dev.aurelium.auraskills.fabric.menu;

import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.command.FabricSourceSortType;
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
     * Re-open a list menu ({@link Id#SKILLS} or {@link Id#STATS}) with an explicit
     * {@link FabricMenuContext#listPage()} — used by detail **Back** (Sprint 38).
     */
    public static void open(
            Id id, ServerPlayerEntity player, FabricAuraSkills plugin, FabricMenuContext context) {
        open(id.menuId(), player, plugin, context);
    }

    /**
     * Bukkit id {@code level_progression} (see {@link FabricMenuKeys#LEVEL_PROGRESSION}): {@link Skill}
     * context is required; routes through {@link FabricMenuRegistry}.
     */
    public static void openLevelProgression(Skill skill, ServerPlayerEntity player, FabricAuraSkills plugin) {
        openLevelProgression(skill, player, plugin, 0);
    }

    /**
     * Same as {@link #openLevelProgression(Skill, ServerPlayerEntity, FabricAuraSkills)} but records the
     * paged <strong>Skills</strong> list page to return to on <strong>Back</strong> (0-based, clamped in
     * the list GUI after open).
     */
    public static void openLevelProgression(
            Skill skill, ServerPlayerEntity player, FabricAuraSkills plugin, int returnSkillsListPage) {
        open(
                FabricMenuKeys.LEVEL_PROGRESSION,
                player,
                plugin,
                FabricMenuContext.ofSkill(skill).withListPage(returnSkillsListPage));
    }

    /**
     * Bukkit id {@code stat_info} (see {@link FabricMenuKeys#STAT_INFO}): {@link Stat} context is required;
     * still gated in {@code FabricStatInfoMenuGui} by {@code menus.stat_info.enabled}. Routes through
     * {@link FabricMenuRegistry}.
     */
    public static void openStatInfo(Stat stat, ServerPlayerEntity player, FabricAuraSkills plugin) {
        openStatInfo(stat, player, plugin, 0);
    }

    /**
     * Same as {@link #openStatInfo(Stat, ServerPlayerEntity, FabricAuraSkills)} but records the paged
     * <strong>Stats</strong> list page to return to on <strong>Back</strong> (0-based, clamped in the list
     * GUI after open).
     */
    public static void openStatInfo(Stat stat, ServerPlayerEntity player, FabricAuraSkills plugin, int returnStatsListPage) {
        open(
                FabricMenuKeys.STAT_INFO,
                player,
                plugin,
                FabricMenuContext.ofStat(stat).withListPage(returnStatsListPage));
    }

    /**
     * One-skill XP sources list (Bukkit id {@code sources}), opened from {@code /sources} (**Sprint
     * 69**), **{@code /openmenu sources …}** (**Sprint 70**), and the <strong>level progression</strong>
     * screen hopper (**Sprint 70**). Not in {@link FabricMenuRegistry} / {@link FabricMenuLoaders}. {@code
     * returnToSkillsListPage} is the Skills list page passed to
     * {@link #openLevelProgression(Skill, ServerPlayerEntity, FabricAuraSkills, int)} on <strong>Back</strong>
     * (Bukkit: previous menu <strong>level_progression</strong>).
     */
    public static void openSources(
            Skill skill,
            ServerPlayerEntity player,
            FabricAuraSkills plugin,
            FabricSourceSortType sort,
            int page,
            int returnToSkillsListPage) {
        FabricSourcesMenuGui.open(player, plugin, skill, sort, page, returnToSkillsListPage);
    }

    /**
     * Unclaimed registered item rewards (Bukkit {@code UnclaimedItemsMenu}), **Sprint 71**. Not in
     * {@link FabricMenuRegistry} / YAML loaders.
     */
    public static void openUnclaimedItems(ServerPlayerEntity player, FabricAuraSkills plugin, int page) {
        FabricUnclaimedItemsMenuGui.open(player, plugin, page);
    }
}
