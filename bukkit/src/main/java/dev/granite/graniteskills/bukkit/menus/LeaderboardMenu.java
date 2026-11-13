package dev.granite.graniteskills.bukkit.menus;

import dev.aurelium.slate.builder.MenuBuilder;
import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.api.skill.Skills;
import dev.granite.graniteskills.bukkit.GraniteSkills;
import dev.granite.graniteskills.bukkit.menus.shared.GlobalItems;
import dev.granite.graniteskills.bukkit.util.VersionUtils;
import dev.granite.graniteskills.common.leaderboard.LeaderboardManager;
import dev.granite.graniteskills.common.leaderboard.SkillValue;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LeaderboardMenu {

    private final GraniteSkills plugin;

    public LeaderboardMenu(GraniteSkills plugin) {
        this.plugin = plugin;
    }

    public void build(MenuBuilder menu) {
        menu.replace("skill", p -> ((Skill) p.menu().getProperty("skill")).getDisplayName(p.locale(), false));

        menu.properties(m -> Map.of(
                "skill", m.menu().getProperty("skill", Skills.FARMING),
                "previous_menu", "level_progression"));

        var globalItems = new GlobalItems(plugin);
        menu.item("back", globalItems::backToLevelProgression);
        menu.fillItem(globalItems::fill);

        menu.template("leaderboard_player", Integer.class, template -> {
            LeaderboardManager lb = plugin.getLeaderboardManager();
            template.replace("place", p -> String.valueOf(p.value()));
            template.replace("player", p -> {
                SkillValue value = lb.getSkillValue((Skill) p.menu().getProperty("skill"), p.value());
                String name = Bukkit.getOfflinePlayer(value != null ? value.id() : UUID.randomUUID()).getName();
                return name != null ? name : "?";
            });
            template.replace("level", p -> {
                SkillValue value = lb.getSkillValue((Skill) p.menu().getProperty("skill"), p.value());
                return String.valueOf(value != null ? value.level() : 0);
            });
            // Places 1-10
            template.definedContexts(m -> IntStream.rangeClosed(1, 10).boxed().collect(Collectors.toSet()));
            // Apply player skin to player head
            template.modify(t -> {
                Skill skill = (Skill) t.menu().getProperty("skill");
                SkillValue value = lb.getSkillValue(skill, t.value());
                if (value == null) return null;
                if (t.item().getItemMeta() instanceof SkullMeta meta) {
                    setSkullTexture(meta, value);
                    t.item().setItemMeta(meta);
                }
                return t.item();
            });
        });
    }

    private void setSkullTexture(SkullMeta meta, SkillValue value) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(value.id());
        if (VersionUtils.isAtLeastVersion(18, 1)) {
            if (!offlinePlayer.getPlayerProfile().isComplete()) {
                return;
            }
        }
        meta.setOwningPlayer(offlinePlayer);
    }

}
