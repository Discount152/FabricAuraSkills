package dev.aurelium.auraskills.fabric.damage;

import dev.aurelium.auraskills.api.damage.DamageCalculator;
import dev.aurelium.auraskills.api.damage.DamageType;
import dev.aurelium.auraskills.api.source.type.DamageXpSource.DamageCause;
import dev.aurelium.auraskills.fabric.FabricAbilityManager;
import dev.aurelium.auraskills.fabric.FabricAuraSkills;
import dev.aurelium.auraskills.fabric.FabricDamageCauseMapper;
import dev.aurelium.auraskills.fabric.FabricUser;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import org.jetbrains.annotations.Nullable;

/**
 * Ports Bukkit {@link dev.aurelium.auraskills.bukkit.damage.DamageHandler} + {@link dev.aurelium.auraskills.bukkit.listeners.CriticalHandler}
 * attack/defense trait stages for incoming {@link LivingEntity#damage} calls.
 */
public final class FabricDamagePipeline {

    private FabricDamagePipeline() {
    }

    public static float modifyIncomingDamage(ServerWorld world, LivingEntity target, DamageSource source, float amount) {
        if (amount <= 0f) {
            return amount;
        }
        FabricAuraSkills plugin = FabricAuraSkills.getInstance();
        if (plugin == null) {
            return amount;
        }

        ServerPlayerEntity attackingPlayer = resolveAttackingPlayer(source);
        ServerPlayerEntity targetPlayer = target instanceof ServerPlayerEntity sp ? sp : null;

        if (attackingPlayer == null && targetPlayer == null) {
            return amount;
        }

        if (attackingPlayer != null) {
            if (FabricDamageCauseMapper.toApiCause(source) == DamageCause.THORNS) {
                return amount;
            }
            String w = FabricUser.worldNameCompat(attackingPlayer);
            if (plugin.getWorldManager().isDisabledWorld(w)) {
                return amount;
            }
        }
        if (targetPlayer != null) {
            String w = FabricUser.worldNameCompat(targetPlayer);
            if (plugin.getWorldManager().isDisabledWorld(w)) {
                return amount;
            }
        }

        Entity directAttacker = source.getAttacker();
        DamageType skillsType = FabricDamageTypeResolver.resolve(directAttacker, attackingPlayer);

        FabricDamageMeta meta = new FabricDamageMeta(directAttacker, target, skillsType, source, amount, "vanilla");

        FabricAbilityManager abilities = (FabricAbilityManager) plugin.getAbilityManager();

        if (attackingPlayer != null) {
            FabricCriticalHandler.apply(plugin, meta, attackingPlayer);
            abilities.applyAbilityAttackDamage(meta, attackingPlayer);
            FabricAttackDamageApplier.apply(plugin, meta, attackingPlayer);
        }
        if (targetPlayer != null) {
            abilities.applyAbilityDefenseDamage(meta, targetPlayer);
            FabricDamageReductionApplier.apply(plugin, meta, targetPlayer);
        }

        double modified = DamageCalculator.computeModifiedDamage(
                meta.getBaseDamage(),
                meta.getAttackModifiers(),
                meta.getDefenseModifiers());

        if (Double.isNaN(modified) || Double.isInfinite(modified)) {
            return amount;
        }
        return Math.max(0f, (float) modified);
    }

    /**
     * Bukkit fighting {@code DamageEvent} only applies Parry when {@code getAttackerAsPlayer() != null}.
     */
    public static boolean hasAttackingPlayer(DamageSource source) {
        return resolveAttackingPlayer(source) != null;
    }

    private static @Nullable ServerPlayerEntity resolveAttackingPlayer(DamageSource source) {
        Entity direct = source.getAttacker();
        if (direct instanceof ServerPlayerEntity sp) {
            return sp;
        }
        if (direct instanceof ProjectileEntity projectile) {
            Entity owner = projectile.getOwner();
            if (owner instanceof ServerPlayerEntity sp) {
                return sp;
            }
        }
        return null;
    }
}
