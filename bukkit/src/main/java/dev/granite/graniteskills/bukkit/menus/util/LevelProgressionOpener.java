package dev.granite.graniteskills.bukkit.menus.util;

import dev.aurelium.slate.menu.LoadedMenu;
import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.bukkit.GraniteSkills;
import dev.granite.graniteskills.common.user.User;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LevelProgressionOpener {

    private final GraniteSkills plugin;

    public LevelProgressionOpener(GraniteSkills plugin) {
        this.plugin = plugin;
    }

    public void open(Player player, Skill skill) {
        User user = plugin.getUser(player);
        // Get items per page from options, default to 24
        int page = getPage(skill, user);
        Map<String, Object> properties = new ConcurrentHashMap<>();
        properties.put("skill", skill);
        properties.put("previous_menu", "skills");
        plugin.getSlate().openMenu(player, "level_progression", properties, page);
    }

    public int getPage(Skill skill, User user) {
        int itemsPerPage = 24;
        int startLevel = 1;
        LoadedMenu menu = plugin.getSlate().getLoadedMenu("level_progression");
        if (menu != null) {
            itemsPerPage = (int) menu.options().getOrDefault("items_per_page", 24);
            startLevel = (int) menu.options().getOrDefault("start_level", 1);
        }

        int page = (user.getSkillLevel(skill) - startLevel + 1) / itemsPerPage;
        int maxLevelPage = (skill.getMaxLevel() - startLevel) / itemsPerPage;
        if (page > maxLevelPage) {
            page = maxLevelPage;
        }
        return page;
    }

}
