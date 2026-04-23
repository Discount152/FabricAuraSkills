package dev.aurelium.auraskills.fabric.trait;

import dev.aurelium.auraskills.api.trait.Trait;
import dev.aurelium.auraskills.api.trait.Traits;
import dev.aurelium.auraskills.api.util.NumberUtil;
import dev.aurelium.auraskills.common.scheduler.TaskRunnable;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.FabricHealthRegen;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Fabric counterpart to Bukkit {@code HealthRegenTraits}:
 * <ul>
 *   <li><strong>Natural regen (default):</strong> {@code HungerManagerHealthRegenMixin} redirects
 *       the two {@code ServerPlayerEntity#heal} calls in {@code HungerManager#update} to add
 *       {@code getBonusTraitLevel} the same way Bukkit’s {@code EntityRegainHealthEvent} handler does.</li>
 *   <li><strong>Custom delay:</strong> periodic tasks (same tick→ms period as Bukkit) call
 *       {@link FabricHealthRegen#customRegenIfEligible} with the same Bukkit conditions.
 *       <strong>Not</strong> available: Bukkit {@code CustomRegenEvent} — api-bukkit only; in-repo
 *       ability listeners (e.g. Endurance/Agility) are not on Fabric until ported.</li>
 * </ul>
 */
public final class FabricHealthRegenTraits extends FabricTraitImpl {

    public FabricHealthRegenTraits(FabricAuraSkills plugin) {
        super(plugin, Traits.HUNGER_REGEN, Traits.SATURATION_REGEN);
        startRegenTasks(plugin);
    }

    @Override
    public double getBaseLevel(ServerPlayerEntity player, Trait trait) {
        return 0.0;
    }

    @Override
    public String getMenuDisplay(double value, Trait trait, Locale locale) {
        return "+" + NumberUtil.format2(value);
    }

    private static void startRegenTasks(FabricAuraSkills plugin) {
        startHungerRegen(plugin);
        startSaturationRegen(plugin);
    }

    private static void startHungerRegen(FabricAuraSkills plugin) {
        Trait trait = Traits.HUNGER_REGEN;
        TaskRunnable task = new TaskRunnable() {
            @Override
            public void run() {
                if (!trait.isEnabled() || !trait.optionBoolean("use_custom_delay", false)) {
                    return;
                }
                for (ServerPlayerEntity player : plugin.getServer().getPlayerManager().getPlayerList()) {
                    FabricHealthRegen.customRegenIfEligible(
                            plugin, player, trait,
                            p -> {
                                int food = p.getHungerManager().getFoodLevel();
                                return food >= 14 && food < 20;
                            });
                }
            }
        };
        long periodMs = Math.max(1L, (long) Traits.HUNGER_REGEN.optionInt("delay", 60) * 50L);
        plugin.getScheduler().timerSync(task, 0, periodMs, TimeUnit.MILLISECONDS);
    }

    private static void startSaturationRegen(FabricAuraSkills plugin) {
        Trait trait = Traits.SATURATION_REGEN;
        TaskRunnable task = new TaskRunnable() {
            @Override
            public void run() {
                if (!trait.isEnabled() || !trait.optionBoolean("use_custom_delay", false)) {
                    return;
                }
                for (ServerPlayerEntity player : plugin.getServer().getPlayerManager().getPlayerList()) {
                    FabricHealthRegen.customRegenIfEligible(
                            plugin, player, trait,
                            p -> p.getHungerManager().getSaturationLevel() >= 0
                                    && p.getHungerManager().getFoodLevel() >= 20);
                }
            }
        };
        long periodMs = Math.max(1L, (long) Traits.SATURATION_REGEN.optionInt("delay", 20) * 50L);
        plugin.getScheduler().timerSync(task, 0, periodMs, TimeUnit.MILLISECONDS);
    }
}
