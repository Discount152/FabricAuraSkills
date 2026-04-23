package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.trait.Trait;
import dev.aurelium.auraskills.api.trait.Traits;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.fabric.trait.FabricTraitWorlds;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.function.Predicate;

/**
 * Bukkit {@code HealthRegenTraits} parity: natural food regen in {@code HungerManager#update} (both
 * {@code ServerPlayerEntity#heal} call sites) plus optional custom-delay regen (no Bukkit
 * {@code CustomRegenEvent} on Fabric — see Javadoc on {@link FabricHealthRegenTraits}).
 */
public final class FabricHealthRegen {

    private FabricHealthRegen() {
    }

    /**
     * Same addition as Bukkit {@code event.setAmount(event.getAmount() + bonus)}.
     */
    public static float addBonusToAmount(float baseAmount, double bonusTraitLevel) {
        return baseAmount + (float) bonusTraitLevel;
    }

    public static void redirectFirstHealInUpdate(ServerPlayerEntity player, float vanillaAmount) {
        redirectSatiatedBranch(player, vanillaAmount, Traits.SATURATION_REGEN);
    }

    public static void redirectSecondHealInUpdate(ServerPlayerEntity player, float vanillaAmount) {
        redirectSatiatedBranch(player, vanillaAmount, Traits.HUNGER_REGEN);
    }

    /**
     * First {@code heal} in {@code HungerManager#update}: high-sat / full food branch.
     * Second {@code heal}: 1 HP on the lower-food branch. Bukkit still maps both to
     * {@code EntityRegainHealthEvent#RegainReason#SATIATED} and chooses trait by saturation vs hunger.
     */
    private static void redirectSatiatedBranch(ServerPlayerEntity player, float vanillaAmount, Trait trait) {
        if (player.getEntityWorld().isClient()) {
            if (vanillaAmount > 0) {
                player.heal(vanillaAmount);
            }
            return;
        }
        FabricAuraSkills plugin = FabricAuraSkills.getInstance();
        if (plugin == null) {
            if (vanillaAmount > 0) {
                player.heal(vanillaAmount);
            }
            return;
        }
        if (player.isDead() || !player.isAlive()) {
            if (vanillaAmount > 0) {
                player.heal(vanillaAmount);
            }
            return;
        }
        if (FabricTraitWorlds.isInDisabledWorld(plugin, player)) {
            if (vanillaAmount > 0) {
                player.heal(vanillaAmount);
            }
            return;
        }
        if (player.isSpectator() || !player.canFoodHeal()) {
            if (vanillaAmount > 0) {
                player.heal(vanillaAmount);
            }
            return;
        }
        if (!trait.isEnabled()) {
            if (vanillaAmount > 0) {
                player.heal(vanillaAmount);
            }
            return;
        }
        if (trait.optionBoolean("use_custom_delay", false)) {
            // Bukkit: cancel vanilla EntityRegainHealthEvent; custom timer applies instead.
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUuid());
        double bonus = user.getBonusTraitLevel(trait);
        float toApply = addBonusToAmount(vanillaAmount, bonus);
        if (toApply > 0) {
            player.heal(toApply);
        }
    }

    public static void customRegenIfEligible(
            FabricAuraSkills plugin,
            ServerPlayerEntity player,
            Trait trait,
            Predicate<ServerPlayerEntity> regenCondition) {
        if (player.getEntityWorld().isClient()) {
            return;
        }
        if (FabricTraitWorlds.isInDisabledWorld(plugin, player)) {
            return;
        }
        if (player.isDead() || !player.isAlive() || player.isSpectator()) {
            return;
        }
        if (!trait.isEnabled() || !trait.optionBoolean("use_custom_delay", false)) {
            return;
        }
        float max = player.getMaxHealth();
        if (!(player.getHealth() < max)) {
            return;
        }
        if (!regenCondition.test(player)) {
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUuid());
        double amountGained = Math.min(
                trait.optionDouble("base") + user.getBonusTraitLevel(trait),
                max - player.getHealth());
        if (amountGained < 0.001) {
            return;
        }
        // Bukkit: CustomRegenEvent (api-bukkit) — not fired on Fabric; no plugin listeners in-repo.
        player.setHealth(player.getHealth() + (float) amountGained);
        var hunger = player.getHungerManager();
        if (hunger.getFoodLevel() - 1 >= 0) {
            hunger.setFoodLevel(hunger.getFoodLevel() - 1);
        }
    }
}
