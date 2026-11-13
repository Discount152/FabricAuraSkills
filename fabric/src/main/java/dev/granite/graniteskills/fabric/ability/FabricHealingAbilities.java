package dev.granite.graniteskills.fabric.ability;

import dev.granite.graniteskills.api.ability.Abilities;
import dev.granite.graniteskills.api.stat.StatModifier;
import dev.granite.graniteskills.api.stat.Stats;
import dev.granite.graniteskills.api.util.GraniteSkillsModifier.Operation;
import dev.granite.graniteskills.api.util.NumberUtil;
import dev.granite.graniteskills.common.message.type.AbilityMessage;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.common.util.text.TextUtil;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;

import java.util.concurrent.TimeUnit;

public final class FabricHealingAbilities extends FabricAbilityImpl {

    private static final String REVIVAL_HEALTH = "GraniteSkills.Ability.Revival.Health";
    private static final String REVIVAL_REGEN = "GraniteSkills.Ability.Revival.Regeneration";

    public FabricHealingAbilities(FabricGraniteSkills plugin) {
        super(plugin, Abilities.LIFE_ESSENCE, Abilities.HEALER, Abilities.LIFE_STEAL, Abilities.GOLDEN_HEART, Abilities.REVIVAL);
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> revival(newPlayer));
        ServerPlayerEvents.LEAVE.register(player -> clearRevival(player));
    }

    public static float adjustHeal(ServerPlayer player, float amount) {
        FabricGraniteSkills plugin = FabricGraniteSkills.getInstance();
        if (plugin == null || amount <= 0f || !player.hasEffect(MobEffects.INSTANT_HEALTH)) {
            return amount;
        }
        FabricHealingAbilities self = plugin.getAbilityManager().getAbilityImpl(FabricHealingAbilities.class);
        var ability = Abilities.LIFE_ESSENCE;
        if (self.isDisabled(ability) || self.failsChecks(player, ability)) {
            return amount;
        }
        User user = plugin.getUserManager().getUser(player.getUUID());
        return (float) (amount * (1.0 + self.getValue(ability, user) / 100.0));
    }

    public static float goldenHeartMultiplier(ServerPlayer player) {
        FabricGraniteSkills plugin = FabricGraniteSkills.getInstance();
        if (plugin == null || player.getAbsorptionAmount() <= 0f) {
            return 1f;
        }
        FabricHealingAbilities self = plugin.getAbilityManager().getAbilityImpl(FabricHealingAbilities.class);
        var ability = Abilities.GOLDEN_HEART;
        if (self.isDisabled(ability) || self.failsChecks(player, ability)) {
            return 1f;
        }
        User user = plugin.getUserManager().getUser(player.getUUID());
        double multiplier = 1.0 - self.getValue(ability, user) / 100.0;
        return (float) Math.max(0.01, multiplier);
    }

    private void revival(ServerPlayer player) {
        var ability = Abilities.REVIVAL;
        if (isDisabled(ability) || failsChecks(player, ability)) {
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUUID());
        double healthBonus = getValue(ability, user);
        double regenerationBonus = getSecondaryValue(ability, user);
        user.addStatModifier(new StatModifier(REVIVAL_HEALTH, Stats.HEALTH, healthBonus, Operation.ADD));
        user.addStatModifier(new StatModifier(REVIVAL_REGEN, Stats.REGENERATION, regenerationBonus, Operation.ADD));
        if (ability.optionBoolean("enable_message", true)) {
            plugin.getAbilityManager().sendAbilityMessage(user, TextUtil.replace(
                    plugin.getMsg(AbilityMessage.REVIVAL_MESSAGE, user.getLocale()),
                    "{value}", NumberUtil.format1(healthBonus),
                    "{value_2}", NumberUtil.format1(regenerationBonus)));
        }
        plugin.getScheduler().scheduleSync(() -> clearRevival(player), 30, TimeUnit.SECONDS);
    }

    private void clearRevival(ServerPlayer player) {
        User user = plugin.getUserManager().getUser(player.getUUID());
        user.removeStatModifier(REVIVAL_HEALTH);
        user.removeStatModifier(REVIVAL_REGEN);
    }
}
