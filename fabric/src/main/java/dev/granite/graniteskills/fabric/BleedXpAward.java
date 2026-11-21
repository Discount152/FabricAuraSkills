package dev.granite.graniteskills.fabric;

import dev.granite.graniteskills.api.source.type.DamageXpSource.DamageCause;
import dev.granite.graniteskills.api.source.type.EntityXpSource;

/**
 * Entity XP for a bleed tick. The source must be a damage trigger for that entity, damager, and
 * cause. Anything else awards nothing.
 */
public final class BleedXpAward {

    private BleedXpAward() {
    }

    public static boolean matches(String victimEntity, String sourceEntity, EntityXpSource.EntityTriggers[] triggers,
            EntityXpSource.EntityDamagers eventDamager, EntityXpSource.EntityDamagers[] sourceDamagers,
            DamageCause eventCause, DamageCause[] causes, DamageCause[] excludedCauses) {
        if (!hasDamageTrigger(triggers) || victimEntity == null || sourceEntity == null
                || !victimEntity.equalsIgnoreCase(sourceEntity)) {
            return false;
        }
        if (causes != null) {
            boolean causeOk = false;
            for (DamageCause cause : causes) {
                if (cause != null && eventCause != null && cause.toString().equals(eventCause.toString())) {
                    causeOk = true;
                    break;
                }
            }
            if (!causeOk) {
                return false;
            }
        }
        if (excludedCauses != null && eventCause != null) {
            for (DamageCause cause : excludedCauses) {
                if (cause != null && cause.toString().equals(eventCause.toString())) {
                    return false;
                }
            }
        }
        if (sourceDamagers == null || eventDamager == null) {
            return false;
        }
        for (EntityXpSource.EntityDamagers damager : sourceDamagers) {
            if (damager == eventDamager) {
                return true;
            }
        }
        return false;
    }

    public static double xp(boolean sourceMatches, double damageTaken, double sourceXp) {
        if (!sourceMatches || damageTaken <= 0.0) {
            return 0.0;
        }
        return damageTaken * sourceXp;
    }

    private static boolean hasDamageTrigger(EntityXpSource.EntityTriggers[] triggers) {
        if (triggers == null) {
            return false;
        }
        for (EntityXpSource.EntityTriggers trigger : triggers) {
            if (trigger == EntityXpSource.EntityTriggers.DAMAGE) {
                return true;
            }
        }
        return false;
    }
}
