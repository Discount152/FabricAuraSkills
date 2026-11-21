package dev.granite.graniteskills.fabric.ability;

import dev.granite.graniteskills.api.ability.Abilities;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import net.minecraft.server.level.ServerPlayer;

public final class FabricEnduranceAbilities extends FabricAbilityImpl {

    public FabricEnduranceAbilities(FabricGraniteSkills plugin) {
        super(plugin, Abilities.ANTI_HUNGER, Abilities.RUNNER, Abilities.GOLDEN_HEAL, Abilities.RECOVERY, Abilities.MEAL_STEAL);
    }

    public static boolean shouldIgnoreHunger(ServerPlayer player) {
        FabricGraniteSkills plugin = FabricGraniteSkills.getInstance();
        if (plugin == null) {
            return false;
        }
        FabricEnduranceAbilities self = plugin.getAbilityManager().getAbilityImpl(FabricEnduranceAbilities.class);
        return shouldIgnoreHunger(player, self.rand.nextDouble());
    }

    /**
     * Deterministic Anti Hunger check (Bukkit {@code FoodLevelChangeEvent} chance). Used by probes and by
     * {@link #shouldIgnoreHunger(ServerPlayer)} with a live roll.
     */
    public static boolean shouldIgnoreHunger(ServerPlayer player, double roll) {
        FabricGraniteSkills plugin = FabricGraniteSkills.getInstance();
        if (plugin == null || player == null) {
            return false;
        }
        FabricEnduranceAbilities self = plugin.getAbilityManager().getAbilityImpl(FabricEnduranceAbilities.class);
        var ability = Abilities.ANTI_HUNGER;
        if (self.isDisabled(ability) || self.failsChecks(player, ability)) {
            return false;
        }
        User user = plugin.getUserManager().getUser(player.getUUID());
        return roll < self.getValue(ability, user) / 100.0;
    }

    public static void onPlayerMelee(ServerPlayer attacker, ServerPlayer victim) {
        applyMealSteal(attacker, victim, Double.NaN);
    }

    /**
     * Bukkit meal steal: chance to take 1 food from a player victim on melee hit.
     *
     * @param forcedRoll pass a fixed {@code [0,1)} roll for probes; {@link Double#NaN} uses the ability RNG
     * @return true when food was transferred
     */
    public static boolean applyMealSteal(ServerPlayer attacker, ServerPlayer victim, double forcedRoll) {
        FabricGraniteSkills plugin = FabricGraniteSkills.getInstance();
        if (plugin == null || attacker == null || victim == null) {
            return false;
        }
        FabricEnduranceAbilities self = plugin.getAbilityManager().getAbilityImpl(FabricEnduranceAbilities.class);
        var ability = Abilities.MEAL_STEAL;
        if (self.isDisabled(ability) || self.failsChecks(attacker, ability)) {
            return false;
        }
        User user = plugin.getUserManager().getUser(attacker.getUUID());
        double roll = Double.isNaN(forcedRoll) ? self.rand.nextDouble() : forcedRoll;
        if (roll >= self.getValue(ability, user) / 100.0) {
            return false;
        }
        boolean transferred = false;
        if (victim.getFoodData().getFoodLevel() >= 1) {
            victim.getFoodData().setFoodLevel(victim.getFoodData().getFoodLevel() - 1);
            transferred = true;
        }
        if (attacker.getFoodData().getFoodLevel() < 20) {
            attacker.getFoodData().setFoodLevel(attacker.getFoodData().getFoodLevel() + 1);
            transferred = true;
        } else if (attacker.getFoodData().getSaturationLevel() < 20f) {
            attacker.getFoodData().setSaturation(attacker.getFoodData().getSaturationLevel() + 1f);
            transferred = true;
        }
        return transferred;
    }

    public static float adjustHeal(ServerPlayer player, float amount) {
        FabricGraniteSkills plugin = FabricGraniteSkills.getInstance();
        if (plugin == null || amount <= 0f) {
            return amount;
        }
        FabricEnduranceAbilities self = plugin.getAbilityManager().getAbilityImpl(FabricEnduranceAbilities.class);
        User user = plugin.getUserManager().getUser(player.getUUID());
        if (player.hasEffect(net.minecraft.world.effect.MobEffects.REGENERATION)) {
            var ability = Abilities.GOLDEN_HEAL;
            if (!self.isDisabled(ability) && !self.failsChecks(player, ability)) {
                amount = (float) (amount * (1.0 + self.getValue(ability, user) / 100.0));
            }
        } else if (player.getFoodData().getFoodLevel() >= 18 && player.getHealth() < player.getMaxHealth() / 2f) {
            var ability = Abilities.RECOVERY;
            if (!self.isDisabled(ability) && !self.failsChecks(player, ability)) {
                amount = (float) (amount * (1.0 + self.getValue(ability, user) / 100.0));
            }
        }
        return amount;
    }
}
