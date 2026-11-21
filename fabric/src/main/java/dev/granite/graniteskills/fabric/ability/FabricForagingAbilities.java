package dev.granite.graniteskills.fabric.ability;

import dev.granite.graniteskills.api.ability.Abilities;
import dev.granite.graniteskills.api.damage.DamageModifier;
import dev.granite.graniteskills.api.damage.DamageType;
import dev.granite.graniteskills.api.stat.StatModifier;
import dev.granite.graniteskills.api.stat.Stats;
import dev.granite.graniteskills.api.util.GraniteSkillsModifier.Operation;
import dev.granite.graniteskills.common.scheduler.TaskRunnable;
import dev.granite.graniteskills.common.user.User;
import dev.granite.graniteskills.fabric.FabricGraniteSkills;
import dev.granite.graniteskills.fabric.damage.FabricDamageMeta;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.concurrent.TimeUnit;

public final class FabricForagingAbilities extends FabricAbilityImpl implements FabricDamagePipelineContributor {

    private static final String VALOR_MODIFIER = "foraging-valor";

    public FabricForagingAbilities(FabricGraniteSkills plugin) {
        super(plugin, Abilities.LUMBERJACK, Abilities.FORAGER, Abilities.AXE_MASTER, Abilities.SHREDDER, Abilities.VALOR);
        plugin.getScheduler().timerSync(new TaskRunnable() {
            @Override
            public void run() {
                refreshValor();
            }
        }, 1, 10, TimeUnit.SECONDS);
    }

    @Override
    public void contributeAttackDamage(FabricGraniteSkills plugin, FabricDamageMeta meta, ServerPlayer attacker) {
        if (meta.getDamageType() != DamageType.AXE) {
            return;
        }
        User user = plugin.getUserManager().getUser(attacker.getUUID());
        meta.addAttackModifier(axeMaster(attacker, user));
    }

    private DamageModifier axeMaster(ServerPlayer player, User user) {
        var ability = Abilities.AXE_MASTER;
        if (isDisabled(ability) || failsChecks(player, ability)) {
            return DamageModifier.none();
        }
        if (user.getAbilityLevel(ability) <= 0) {
            return DamageModifier.none();
        }
        return new DamageModifier(getValue(ability, user) / 100.0, DamageModifier.Operation.ADD_COMBINED);
    }

    /**
     * Bukkit Shredder: chance to triple armor durability loss when the attacker hits with an axe.
     *
     * @param forcedRoll {@link Double#NaN} uses live RNG
     * @return true when durability damage should be tripled
     */
    public static boolean applyShredder(
            ServerPlayer attacker, LivingEntity victim, ItemStack armorPiece, double forcedRoll) {
        FabricGraniteSkills plugin = FabricGraniteSkills.getInstance();
        if (plugin == null || attacker == null || victim == null || armorPiece == null || armorPiece.isEmpty()) {
            return false;
        }
        if (!FabricMiningAbilities.isArmor(armorPiece)) {
            return false;
        }
        if (!attacker.getMainHandItem().is(ItemTags.AXES)) {
            return false;
        }
        FabricForagingAbilities self = plugin.getAbilityManager().getAbilityImpl(FabricForagingAbilities.class);
        var ability = Abilities.SHREDDER;
        if (self.isDisabled(ability) || self.failsChecks(attacker, ability)) {
            return false;
        }
        User user = plugin.getUserManager().getUser(attacker.getUUID());
        if (user.getAbilityLevel(ability) <= 0) {
            return false;
        }
        double roll = Double.isNaN(forcedRoll) ? self.rand.nextDouble() : forcedRoll;
        return roll < self.getValue(ability, user) / 100.0;
    }

    /** Probe/runtime refresh: holding an axe grants Strength from Valor. */
    public static void refreshValor(ServerPlayer player) {
        FabricGraniteSkills plugin = FabricGraniteSkills.getInstance();
        if (plugin == null || player == null) {
            return;
        }
        FabricForagingAbilities self = plugin.getAbilityManager().getAbilityImpl(FabricForagingAbilities.class);
        self.refreshValorFor(player);
    }

    private void refreshValor() {
        if (plugin.getServer() == null) {
            return;
        }
        for (ServerPlayer player : plugin.getServer().getPlayerList().getPlayers()) {
            refreshValorFor(player);
        }
    }

    private void refreshValorFor(ServerPlayer player) {
        User user = plugin.getUserManager().getUser(player.getUUID());
        if (user == null) {
            return;
        }
        var ability = Abilities.VALOR;
        if (isDisabled(ability)
                || failsChecks(player, ability)
                || user.getAbilityLevel(ability) <= 0
                || !player.getMainHandItem().is(ItemTags.AXES)) {
            user.removeStatModifier(VALOR_MODIFIER);
            return;
        }
        user.addStatModifier(
                new StatModifier(VALOR_MODIFIER, Stats.STRENGTH, (int) getValue(ability, user), Operation.ADD),
                false);
    }
}
