package dev.granite.graniteskills.fabric.menu;

import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import dev.granite.graniteskills.fabric.command.FabricSourceSortType;
import dev.granitelib.fabric.menu.GraniteMenuHistory;
import net.minecraft.server.level.ServerPlayer;

/**
 * Opens the previous menu recorded in {@link GraniteMenuHistory}, including its skill, page, and sort.
 */
public final class FabricMenuNavigation {

    private FabricMenuNavigation() {
    }

    public static void remember(ServerPlayer player, String menuId, String skillKey, int page, String sort) {
        if (player == null) {
            return;
        }
        GraniteMenuHistory.remember(player.getUUID(), new GraniteMenuHistory.Place(menuId, skillKey, page, sort));
    }

    /**
     * @return true when a previous place was opened
     */
    public static boolean back(ServerPlayer player, FabricGraniteSkills plugin) {
        if (player == null || plugin == null) {
            return false;
        }
        GraniteMenuHistory.Place previous = GraniteMenuHistory.backPlace(player.getUUID());
        if (previous == null) {
            return false;
        }
        openPlace(player, plugin, previous);
        return true;
    }

    private static void openPlace(ServerPlayer player, FabricGraniteSkills plugin, GraniteMenuHistory.Place place) {
        String id = place.menuId();
        Skill skill = skill(plugin, place.skillKey());
        switch (id) {
            case "sources" -> {
                if (skill == null) {
                    FabricGraniteSkillsMenus.open(FabricMenuKeys.SKILLS, player, plugin, FabricMenuContext.none().withListPage(place.page()));
                    return;
                }
                FabricSourceSortType sort = FabricSourceSortType.fromTokenStrict(place.sort()).orElse(FabricSourceSortType.ASCENDING);
                FabricGraniteSkillsMenus.openSources(skill, player, plugin, sort, place.page(), 0);
            }
            case "leaderboard" -> {
                FabricLeaderboardMenuGui.Mode mode = mode(place.sort());
                FabricGraniteSkillsMenus.openLeaderboard(player, plugin, mode, skill, Math.max(1, place.page()));
            }
            case "level_progression" -> {
                if (skill == null) {
                    FabricGraniteSkillsMenus.open(FabricMenuKeys.SKILLS, player, plugin, FabricMenuContext.none().withListPage(place.page()));
                    return;
                }
                FabricGraniteSkillsMenus.open(FabricMenuKeys.LEVEL_PROGRESSION, player, plugin,
                        FabricMenuContext.ofSkill(skill)
                                .withListPage(place.page())
                                .withProgressionPage(progressionPage(place.sort())));
            }
            case "stats" -> FabricGraniteSkillsMenus.open(
                    FabricMenuKeys.STATS, player, plugin, FabricMenuContext.none().withListPage(place.page()));
            case "skills" -> FabricGraniteSkillsMenus.open(
                    FabricMenuKeys.SKILLS, player, plugin, FabricMenuContext.none().withListPage(place.page()));
            default -> FabricGraniteSkillsMenus.open(id, player, plugin);
        }
    }

    private static Skill skill(FabricGraniteSkills plugin, String skillKey) {
        if (skillKey == null || skillKey.isEmpty()) {
            return null;
        }
        return plugin.getSkillRegistry().getFromKey(skillKey);
    }

    private static int progressionPage(String sort) {
        try {
            return Math.max(0, Integer.parseInt(sort));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private static FabricLeaderboardMenuGui.Mode mode(String sort) {
        if (sort == null || sort.isEmpty()) {
            return FabricLeaderboardMenuGui.Mode.POWER;
        }
        try {
            return FabricLeaderboardMenuGui.Mode.valueOf(sort);
        } catch (IllegalArgumentException ignored) {
            return FabricLeaderboardMenuGui.Mode.POWER;
        }
    }
}
