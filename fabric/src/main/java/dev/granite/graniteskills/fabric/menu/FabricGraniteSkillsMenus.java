package dev.granite.graniteskills.fabric.menu;

import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.api.stat.Stat;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import dev.granite.graniteskills.fabric.command.FabricSourceSortType;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

/**
 * Dispatch helpers for built-in menus. Ordinary opens go through
 * {@link FabricGraniteLibMenuBridge} (library registry + TemplateSupport gameplay GUIs) so
 * {@code lastOpenedId}, {@code loadDefinition}, paging, sorting, and claims stay aligned.
 */
public final class FabricGraniteSkillsMenus {

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

    private FabricGraniteSkillsMenus() {
    }

    public static void open(String menuId, ServerPlayer player, FabricGraniteSkills plugin) {
        open(menuId, player, plugin, FabricMenuContext.none());
    }

    public static void open(
            String menuId,
            ServerPlayer player,
            FabricGraniteSkills plugin,
            FabricMenuContext context) {
        FabricMenuContext ctx = context == null ? FabricMenuContext.none() : context;
        String skillKey = "";
        int page = ctx.listPage();
        if (ctx.skill() != null) {
            skillKey = ctx.skill().getId().getSimpleName();
        } else if (ctx.stat() != null) {
            skillKey = ctx.stat().getId().getSimpleName();
        }
        // Library registry + TemplateSupport path (R1).
        plugin.getGraniteLibMenus().open(menuId, player, ctx, "", page);
        // level_progression has no sort; its sort slot carries the progression page so Back restores both pages.
        String historySort = FabricMenuKeys.LEVEL_PROGRESSION.equals(menuId) ? Integer.toString(ctx.progressionPage()) : "";
        FabricMenuNavigation.remember(player, menuId, skillKey, page, historySort);
    }

    /**
     * Gameplay open without re-entering {@link FabricGraniteLibMenuBridge#open} (avoids recursion).
     */
    static void openGameplay(
            String menuId, ServerPlayer player, FabricGraniteSkills plugin, FabricMenuContext context) {
        FabricMenuContext ctx = context == null ? FabricMenuContext.none() : context;
        plugin.getGraniteLibMenus().markOpened(menuId);
        if (plugin.getMenuRegistry() != null && plugin.getMenuRegistry().get(menuId).isPresent()) {
            plugin.getMenuRegistry().open(menuId, player, plugin, ctx);
            return;
        }
        switch (menuId == null ? "" : menuId.toLowerCase(java.util.Locale.ROOT)) {
            case FabricMenuKeys.SOURCES -> {
                // Context-only fallback; typed helpers supply skill/sort.
            }
            default -> plugin.logger().warn("No gameplay opener for menu id '" + menuId + "'");
        }
    }

    public static void open(Id id, ServerPlayer player, FabricGraniteSkills plugin) {
        open(id.menuId(), player, plugin);
    }

    public static void open(
            Id id, ServerPlayer player, FabricGraniteSkills plugin, FabricMenuContext context) {
        open(id.menuId(), player, plugin, context);
    }

    public static void openLevelProgression(Skill skill, ServerPlayer player, FabricGraniteSkills plugin) {
        openLevelProgression(skill, player, plugin, 0);
    }

    public static void openLevelProgression(
            Skill skill, ServerPlayer player, FabricGraniteSkills plugin, int returnSkillsListPage) {
        open(
                FabricMenuKeys.LEVEL_PROGRESSION,
                player,
                plugin,
                FabricMenuContext.ofSkill(skill).withListPage(returnSkillsListPage));
    }

    static void openGameplayLevelProgression(
            ServerPlayer player, FabricGraniteSkills plugin, FabricMenuContext context) {
        plugin.getGraniteLibMenus().markOpened(FabricMenuKeys.LEVEL_PROGRESSION);
        plugin.getMenuRegistry().open(FabricMenuKeys.LEVEL_PROGRESSION, player, plugin, context);
    }

    public static void openStatInfo(Stat stat, ServerPlayer player, FabricGraniteSkills plugin) {
        openStatInfo(stat, player, plugin, 0);
    }

    public static void openStatInfo(
            Stat stat, ServerPlayer player, FabricGraniteSkills plugin, int returnStatsListPage) {
        open(
                FabricMenuKeys.STAT_INFO,
                player,
                plugin,
                FabricMenuContext.ofStat(stat).withListPage(returnStatsListPage));
    }

    static void openGameplayStatInfo(
            Stat stat, ServerPlayer player, FabricGraniteSkills plugin, int returnStatsListPage) {
        plugin.getGraniteLibMenus().markOpened(FabricMenuKeys.STAT_INFO);
        plugin.getMenuRegistry()
                .open(
                        FabricMenuKeys.STAT_INFO,
                        player,
                        plugin,
                        FabricMenuContext.ofStat(stat).withListPage(returnStatsListPage));
    }

    public static void openSources(
            Skill skill,
            ServerPlayer player,
            FabricGraniteSkills plugin,
            FabricSourceSortType sort,
            int page,
            int returnToSkillsListPage) {
        plugin.getGraniteLibMenus()
                .open(
                        FabricMenuKeys.SOURCES,
                        player,
                        FabricMenuContext.ofSkill(skill).withListPage(returnToSkillsListPage),
                        sort == null ? "" : sort.name(),
                        page);
        FabricMenuNavigation.remember(
                player, FabricMenuKeys.SOURCES, skill.getId().getSimpleName(), page, sort == null ? "" : sort.name());
    }

    static void openGameplaySources(
            Skill skill,
            ServerPlayer player,
            FabricGraniteSkills plugin,
            FabricSourceSortType sort,
            int page,
            int returnToSkillsListPage) {
        plugin.getGraniteLibMenus().markOpened(FabricMenuKeys.SOURCES);
        FabricSourcesMenuGui.open(player, plugin, skill, sort, page, returnToSkillsListPage);
    }

    public static void openUnclaimedItems(ServerPlayer player, FabricGraniteSkills plugin, int page) {
        plugin.getGraniteLibMenus()
                .open(FabricMenuKeys.UNCLAIMED_ITEMS, player, FabricMenuContext.none(), "", page);
        FabricMenuNavigation.remember(player, FabricMenuKeys.UNCLAIMED_ITEMS, "", page, "");
    }

    static void openGameplayUnclaimedItems(ServerPlayer player, FabricGraniteSkills plugin, int page) {
        plugin.getGraniteLibMenus().markOpened(FabricMenuKeys.UNCLAIMED_ITEMS);
        FabricUnclaimedItemsMenuGui.open(player, plugin, page);
    }

    public static void openLeaderboard(
            ServerPlayer player,
            FabricGraniteSkills plugin,
            FabricLeaderboardMenuGui.Mode mode,
            @Nullable Skill skill,
            int page) {
        FabricMenuContext ctx = skill == null ? FabricMenuContext.none() : FabricMenuContext.ofSkill(skill);
        plugin.getGraniteLibMenus()
                .open(
                        FabricMenuKeys.LEADERBOARD,
                        player,
                        ctx,
                        mode == null ? "" : mode.name(),
                        Math.max(1, page));
        FabricMenuNavigation.remember(
                player,
                FabricMenuKeys.LEADERBOARD,
                skill == null ? "" : skill.getId().getSimpleName(),
                Math.max(1, page),
                mode == null ? "" : mode.name());
    }

    static void openGameplayLeaderboard(
            ServerPlayer player,
            FabricGraniteSkills plugin,
            FabricLeaderboardMenuGui.Mode mode,
            @Nullable Skill skill,
            int page) {
        plugin.getGraniteLibMenus().markOpened(FabricMenuKeys.LEADERBOARD);
        FabricLeaderboardMenuGui.open(player, plugin, mode, skill, page);
    }

    public static void openAbilities(
            Skill skill, ServerPlayer menuPlayer, FabricGraniteSkills plugin, int returnToSkillsListPage) {
        plugin.getGraniteLibMenus()
                .open(
                        FabricMenuKeys.ABILITIES,
                        menuPlayer,
                        FabricMenuContext.ofSkill(skill).withListPage(returnToSkillsListPage),
                        "",
                        0);
        FabricMenuNavigation.remember(
                menuPlayer, FabricMenuKeys.ABILITIES, skill.getId().getSimpleName(), 0, "");
    }

    static void openGameplayAbilities(
            Skill skill, ServerPlayer menuPlayer, FabricGraniteSkills plugin, int returnToSkillsListPage) {
        plugin.getGraniteLibMenus().markOpened(FabricMenuKeys.ABILITIES);
        FabricAbilitiesMenuGui.open(menuPlayer, plugin, skill, returnToSkillsListPage);
    }
}
