package dev.granite.graniteskills.bukkit.jobs;

import dev.granite.graniteskills.api.event.skill.XpGainEvent;
import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.bukkit.GraniteSkills;
import dev.granite.graniteskills.common.config.Option;
import dev.granite.graniteskills.common.user.User;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class JobsListener implements Listener {

    private final GraniteSkills plugin;

    public JobsListener(GraniteSkills plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void blockXpGain(XpGainEvent event) {
        if (!plugin.config().jobSelectionEnabled()) return;
        if (!plugin.configBoolean(Option.JOBS_SELECTION_DISABLE_UNSELECTED_XP)) return;

        Skill skill = event.getSkill();
        User user = plugin.getUser(event.getPlayer());

        if (!user.getJobs().contains(skill)) {
            event.setCancelled(true);
        }
    }

}
