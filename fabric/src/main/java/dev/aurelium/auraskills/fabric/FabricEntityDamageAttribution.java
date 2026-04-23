package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.skill.Skills;
import dev.aurelium.auraskills.api.source.type.DamageXpSource.DamageCause;
import dev.aurelium.auraskills.api.source.type.EntityXpSource.EntityDamagers;
import dev.aurelium.auraskills.common.config.Option;
import dev.aurelium.auraskills.common.util.data.Pair;
import dev.aurelium.auraskills.fabric.ability.FabricBleedListener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Resolves a {@link ServerPlayerEntity} killer for entity-death and mob-skill contexts. Mirrors
 * Bukkit {@code EntityLeveler#onEntityDeath} / {@code getKiller()} behavior: direct player, player-owned
 * {@link ProjectileEntity}, or {@link ThrownItemEntity} (e.g. splash / lingering potion — not a
 * {@link ProjectileEntity} in vanilla), with {@link FabricBleedListener} attribution when the last
 * damage source is generic (e.g. bleed DoT) and no player is in the {@link DamageSource}.
 */
public final class FabricEntityDamageAttribution {

    private FabricEntityDamageAttribution() {
    }

    /**
     * @param server used to look up a bleed applier; may be null if only damage-source fields are used
     */
    public static @Nullable Pair<ServerPlayerEntity, EntityDamagers> resolveKillerOnDeath(
            DamageSource source,
            @Nullable LivingEntity victim,
            @Nullable MinecraftServer server) {
        Entity direct = source.getSource();
        if (direct instanceof ServerPlayerEntity sp) {
            return new Pair<>(sp, EntityDamagers.PLAYER);
        }
        if (direct instanceof ProjectileEntity proj) {
            Entity owner = proj.getOwner();
            if (owner instanceof ServerPlayerEntity sp) {
                return new Pair<>(sp, EntityDamagers.PROJECTILE);
            }
        }
        if (direct instanceof ThrownItemEntity thrown) {
            Entity owner = thrown.getOwner();
            if (owner instanceof ServerPlayerEntity sp) {
                return new Pair<>(sp, EntityDamagers.PROJECTILE);
            }
        }
        Entity attacker = source.getAttacker();
        if (attacker instanceof ServerPlayerEntity sp) {
            return new Pair<>(sp, EntityDamagers.PLAYER);
        }
        if (victim == null || server == null) {
            return null;
        }
        java.util.UUID bleed = FabricBleedListener.getBleedAttackerForVictim(victim);
        if (bleed == null) {
            return null;
        }
        ServerPlayerEntity fromBleed = server.getPlayerManager().getPlayer(bleed);
        if (fromBleed == null) {
            return null;
        }
        return new Pair<>(fromBleed, EntityDamagers.PLAYER);
    }

    /**
     * Bukkit {@code EntityLeveler#resolveDamager(Entity, DamageCause)} for live {@code DAMAGE} trigger hits — no
     * {@code getAttacker()} fallback (reference uses direct {@code getDamager()} / projectile shooter only). Bleed
     * is handled separately via {@link FabricBleedListener}.
     */
    public static @Nullable Pair<ServerPlayerEntity, EntityDamagers> resolveForEntityLevelerDamage(
            DamageSource source,
            FabricAuraSkills plugin) {
        Entity direct = source.getSource();
        if (direct instanceof ServerPlayerEntity sp) {
            DamageCause c = FabricDamageCauseMapper.toApiCause(source);
            if (c != DamageCause.ENTITY_ATTACK && c != DamageCause.ENTITY_SWEEP_ATTACK) {
                return null;
            }
            return new Pair<>(sp, EntityDamagers.PLAYER);
        }
        if (direct instanceof ProjectileEntity proj) {
            if (!(proj.getOwner() instanceof ServerPlayerEntity sp)) {
                return null;
            }
            return new Pair<>(sp, EntityDamagers.PROJECTILE);
        }
        if (direct instanceof PotionEntity potion) {
            if (!(potion.getOwner() instanceof ServerPlayerEntity sp)) {
                return null;
            }
            if (Skills.ALCHEMY.isEnabled() && plugin.configBoolean(Option.SOURCE_ENTITY_GIVE_ALCHEMY_ON_POTION_COMBAT)) {
                return new Pair<>(sp, EntityDamagers.THROWN_POTION);
            }
            return new Pair<>(sp, EntityDamagers.PROJECTILE);
        }
        if (direct instanceof ThrownItemEntity thrown) {
            if (thrown.getOwner() instanceof ServerPlayerEntity sp) {
                return new Pair<>(sp, EntityDamagers.PROJECTILE);
            }
        }
        return null;
    }
}
