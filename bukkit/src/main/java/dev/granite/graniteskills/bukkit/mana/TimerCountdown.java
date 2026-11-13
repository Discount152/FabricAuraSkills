package dev.granite.graniteskills.bukkit.mana;

import dev.granite.graniteskills.api.event.mana.ManaAbilityRefreshEvent;
import dev.granite.graniteskills.api.mana.ManaAbility;
import dev.granite.graniteskills.bukkit.GraniteSkills;
import dev.granite.graniteskills.bukkit.user.BukkitUser;
import dev.granite.graniteskills.common.config.Option;
import dev.granite.graniteskills.common.mana.ManaAbilityData;
import dev.granite.graniteskills.common.scheduler.TaskRunnable;
import dev.granite.graniteskills.common.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

public class TimerCountdown {

    private final GraniteSkills plugin;
    private final int period;

    public TimerCountdown(GraniteSkills plugin) {
        this.plugin = plugin;
        this.period = Math.min(plugin.configInt(Option.MANA_COOLDOWN_TIMER_PERIOD), 1);
        startCountdown();
    }

    public void startCountdown() {
        // Count cooldown by period every period ticks
        plugin.getScheduler().timerSync(new TaskRunnable() {
            @Override
            public void run() {
                countCooldown();
            }
        }, 0, period * 50L, TimeUnit.MILLISECONDS);
        // Count error timer by 1 every second
        plugin.getScheduler().timerSync(new TaskRunnable() {
            @Override
            public void run() {
                countErrorTimer();
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    private void countCooldown() {
        for (User user : plugin.getUserManager().getOnlineUsers()) {
            for (ManaAbilityData data : user.getManaAbilityDataMap().values()) {
                int cooldown = data.getCooldown();
                if (cooldown > period) {
                    data.setCooldown(cooldown - period);
                } else if (cooldown > 0) {
                    // Cooldown is less than or equal to period
                    data.setCooldown(0);

                    callRefreshEvent(user, data.getManaAbility());
                }
            }
        }
    }

    private void countErrorTimer() {
        for (User user : plugin.getUserManager().getOnlineUsers()) {
            for (ManaAbilityData data : user.getManaAbilityDataMap().values()) {
                int errorTimer = data.getErrorTimer();
                if (errorTimer > 0) {
                    data.setErrorTimer(errorTimer - 1);
                }
            }
        }
    }

    private void callRefreshEvent(User user, ManaAbility manaAbility) {
        Player player = ((BukkitUser) user).getPlayer();
        if (player != null) {
            ManaAbilityRefreshEvent event = new ManaAbilityRefreshEvent(player, user.toApi(), manaAbility);
            Bukkit.getPluginManager().callEvent(event);
        }
    }

}
