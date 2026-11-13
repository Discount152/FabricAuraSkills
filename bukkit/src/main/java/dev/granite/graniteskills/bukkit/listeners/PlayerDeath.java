package dev.granite.graniteskills.bukkit.listeners;

import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.bukkit.GraniteSkills;
import dev.granite.graniteskills.common.config.Option;
import dev.granite.graniteskills.common.user.User;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeath implements Listener {

    private final GraniteSkills plugin;

    public PlayerDeath(GraniteSkills plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (plugin.configBoolean(Option.ON_DEATH_RESET_SKILLS) || plugin.configBoolean(Option.ON_DEATH_RESET_XP)) {
            Player player = event.getEntity();
            User user = plugin.getUser(player);
            for (Skill skill : plugin.getSkillRegistry().getValues()) {
                if (plugin.configBoolean(Option.ON_DEATH_RESET_SKILLS)) {
                    user.resetSkill(skill);
                } else if (plugin.configBoolean(Option.ON_DEATH_RESET_XP)) {
                    double ratio = plugin.configDouble(Option.ON_DEATH_RESET_XP_RATIO);
                    if (ratio > 0 && ratio < 1) {
                        user.setSkillXp(skill, user.getSkillXp(skill) * ratio);
                    } else {
                        user.setSkillXp(skill, 0);
                    }
                }
            }
            plugin.getModifierManager().applyModifiers(player, true);
        }
    }

}
