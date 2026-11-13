package dev.granite.graniteskills.bukkit.event;

import dev.granite.graniteskills.api.event.skill.SkillLevelUpEvent;
import dev.granite.graniteskills.api.event.skill.XpGainEvent;
import dev.granite.graniteskills.api.event.user.UserLoadEvent;
import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.api.source.XpSource;
import dev.granite.graniteskills.bukkit.user.BukkitUser;
import dev.granite.graniteskills.common.event.EventHandler;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.common.util.data.Pair;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class BukkitEventHandler implements EventHandler {

    @Override
    public void callUserLoadEvent(User user) {
        Player player = ((BukkitUser) user).getPlayer();
        if (player != null) {
            UserLoadEvent event = new UserLoadEvent(player, user.toApi());
            Bukkit.getPluginManager().callEvent(event);
        }
    }

    @Override
    public void callSkillLevelUpEvent(User user, Skill skill, int level) {
        Player player = ((BukkitUser) user).getPlayer();
        if (player != null) {
            SkillLevelUpEvent event = new SkillLevelUpEvent(player, user.toApi(), skill, level);
            Bukkit.getPluginManager().callEvent(event);
        }
    }

    @Override
    public Pair<Boolean, Double> callXpGainEvent(User user, Skill skill, @Nullable XpSource source, double amount) {
        Player player = ((BukkitUser) user).getPlayer();
        if (player != null) {
            XpGainEvent event = new XpGainEvent(player, user.toApi(), skill, source, amount);
            Bukkit.getPluginManager().callEvent(event);
            return new Pair<>(event.isCancelled(), event.getAmount());
        } else {
            return new Pair<>(false, 0.0);
        }
    }

}
