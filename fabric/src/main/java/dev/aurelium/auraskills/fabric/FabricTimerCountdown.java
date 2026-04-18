package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.common.config.Option;
import dev.aurelium.auraskills.common.mana.ManaAbilityData;
import dev.aurelium.auraskills.common.scheduler.TaskRunnable;
import dev.aurelium.auraskills.common.user.User;

import java.util.concurrent.TimeUnit;

/**
 * Mirrors {@link dev.aurelium.auraskills.bukkit.mana.TimerCountdown}: decrements mana ability cooldowns and error timers
 * for online users. Bukkit also fires {@code ManaAbilityRefreshEvent} when cooldown hits zero; that event lives in
 * {@code api-bukkit} and is not fired on Fabric (no Bukkit event bus).
 * <p>
 * Cooldown tick period matches Bukkit {@code Math.min(config, 1)}: at most one tick per decrement step (config &gt; 1
 * does not slow the timer on Paper either). The scheduler enforces a minimum 1ms period so a config of 0 does not spin.
 */
public final class FabricTimerCountdown {

    private final FabricAuraSkills plugin;
    private final int period;
    /** Ticks removed from mana cooldown each timer tick (Bukkit uses {@code period}; 0 is treated as 1 so cooldowns clear). */
    private final int cooldownStep;

    public FabricTimerCountdown(FabricAuraSkills plugin) {
        this.plugin = plugin;
        this.period = Math.min(plugin.configInt(Option.MANA_COOLDOWN_TIMER_PERIOD), 1);
        this.cooldownStep = this.period <= 0 ? 1 : this.period;
        startCountdown();
    }

    private void startCountdown() {
        plugin.getScheduler().timerSync(new TaskRunnable() {
            @Override
            public void run() {
                countCooldown();
            }
        }, 0, Math.max(1, period) * 50L, TimeUnit.MILLISECONDS);
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
                if (cooldown > cooldownStep) {
                    data.setCooldown(cooldown - cooldownStep);
                } else if (cooldown > 0) {
                    data.setCooldown(0);
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
}
