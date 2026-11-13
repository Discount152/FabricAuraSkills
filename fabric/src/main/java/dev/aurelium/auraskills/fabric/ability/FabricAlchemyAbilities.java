package dev.aurelium.auraskills.fabric.ability;

import dev.aurelium.auraskills.api.ability.Abilities;
import dev.aurelium.auraskills.api.stat.StatModifier;
import dev.aurelium.auraskills.api.stat.Stats;
import dev.aurelium.auraskills.api.util.AuraSkillsModifier.Operation;
import dev.aurelium.auraskills.common.scheduler.TaskRunnable;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public final class FabricAlchemyAbilities extends FabricAbilityImpl {

    private static final String WISE_EFFECT = "AbilityModifier-WiseEffect";

    public FabricAlchemyAbilities(FabricAuraSkills plugin) {
        super(plugin, Abilities.ALCHEMIST, Abilities.BREWER, Abilities.SPLASHER, Abilities.LINGERING, Abilities.WISE_EFFECT);
        plugin.getScheduler().timerSync(new TaskRunnable() {
            @Override
            public void run() {
                refreshWiseEffect();
            }
        }, 1, 10, TimeUnit.SECONDS);
    }

    public static void onKill(FabricAuraSkills plugin, ServerPlayer player, LivingEntity victim) {
        if (player == null || victim == null || player == victim) {
            return;
        }
        boolean hostile = victim instanceof Enemy || victim instanceof ServerPlayer
                || victim.getType() == net.minecraft.world.entity.EntityTypes.PHANTOM;
        if (!hostile || player.getHealth() <= 0f) {
            return;
        }
        FabricAlchemyAbilities self = plugin.getAbilityManager().getAbilityImpl(FabricAlchemyAbilities.class);
        var ability = Abilities.LIFE_STEAL;
        if (self.isDisabled(ability) || self.failsChecks(player, ability)) {
            return;
        }
        User user = plugin.getUserManager().getUser(player.getUUID());
        double healthRegen = victim.getMaxHealth() * (self.getValue(ability, user) / 100.0);
        float max = player.getMaxHealth();
        player.setHealth((float) Math.min(max, player.getHealth() + healthRegen));
    }

    private void refreshWiseEffect() {
        var ability = Abilities.WISE_EFFECT;
        if (plugin.getServer() == null) {
            return;
        }
        for (ServerPlayer player : plugin.getServer().getPlayerList().getPlayers()) {
            User user = plugin.getUserManager().getUser(player.getUUID());
            if (player.getActiveEffects().isEmpty() || isDisabled(ability) || failsChecks(player, ability)) {
                user.removeStatModifier(WISE_EFFECT);
                continue;
            }
            Set<Object> types = new HashSet<>();
            player.getActiveEffects().forEach(effect -> types.add(effect.getEffect()));
            double modifier = getValue(ability, user) * types.size();
            if (modifier > 0) {
                user.addStatModifier(new StatModifier(WISE_EFFECT, Stats.WISDOM, modifier, Operation.ADD), true);
            } else {
                user.removeStatModifier(WISE_EFFECT);
            }
        }
    }
}
