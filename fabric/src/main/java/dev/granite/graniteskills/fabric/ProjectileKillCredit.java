package dev.granite.graniteskills.fabric;

import dev.granite.graniteskills.api.source.type.EntityXpSource;

import java.util.UUID;

/**
 * Chooses who receives entity XP for a hit. A projectile credits its shooter.
 * Another player on the damage record does not receive that XP.
 */
public final class ProjectileKillCredit {

    public record Credit(UUID playerId, EntityXpSource.EntityDamagers damager) {
    }

    private ProjectileKillCredit() {
    }

    public static Credit resolve(boolean directIsProjectile, boolean thrownPotion, UUID projectileOwner, UUID causingPlayer, UUID directPlayer) {
        if (directIsProjectile) {
            UUID shooter = projectileOwner != null ? projectileOwner : causingPlayer;
            if (shooter == null) {
                return null;
            }
            EntityXpSource.EntityDamagers damager = thrownPotion
                    ? EntityXpSource.EntityDamagers.THROWN_POTION
                    : EntityXpSource.EntityDamagers.PROJECTILE;
            return new Credit(shooter, damager);
        }
        if (directPlayer != null) {
            return new Credit(directPlayer, EntityXpSource.EntityDamagers.PLAYER);
        }
        return null;
    }
}
