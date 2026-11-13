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
        var ability = Abilities.ANTI_HUNGER;
        if (self.isDisabled(ability) || self.failsChecks(player, ability)) {
            return false;
        }
        User user = plugin.getUserManager().getUser(player.getUUID());
        return self.rand.nextDouble() < self.getValue(ability, user) / 100.0;
    }

    public static void onPlayerMelee(ServerPlayer attacker, ServerPlayer victim) {
        FabricGraniteSkills plugin = FabricGraniteSkills.getInstance();
        if (plugin == null || attacker == null || victim == null) {
            return;
        }
        FabricEnduranceAbilities self = plugin.getAbilityManager().getAbilityImpl(FabricEnduranceAbilities.class);
        var ability = Abilities.MEAL_STEAL;
        if (self.isDisabled(ability) || self.failsChecks(attacker, ability)) {
            return;
        }
        User user = plugin.getUserManager().getUser(attacker.getUUID());
        if (self.rand.nextDouble() >= self.getValue(ability, user) / 100.0) {
            return;
        }
        if (victim.getFoodData().getFoodLevel() >= 1) {
            victim.getFoodData().setFoodLevel(victim.getFoodData().getFoodLevel() - 1);
        }
        if (attacker.getFoodData().getFoodLevel() < 20) {
            attacker.getFoodData().setFoodLevel(attacker.getFoodData().getFoodLevel() + 1);
        } else if (attacker.getFoodData().getSaturationLevel() < 20f) {
            attacker.getFoodData().setSaturation(attacker.getFoodData().getSaturationLevel() + 1f);
        }
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
