package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.skill.Skills;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * Tracks mobs spawned from vanilla mob spawners so XP can use {@code spawner_multiplier}
 * (see Bukkit {@code EntityLeveler}).
 */
public final class FabricSpawnerMobTracker {

    private static final Set<UUID> SPAWNER_MOBS = ConcurrentHashMap.newKeySet();

    private FabricSpawnerMobTracker() {
    }

    public static void tryMarkFromSpawner(LivingEntity entity) {
        if (!Skills.FIGHTING.isEnabled() && !Skills.ARCHERY.isEnabled()) {
            return;
        }
        if (Skills.FIGHTING.optionDouble("spawner_multiplier", 1) == 1.0
                && Skills.ARCHERY.optionDouble("spawner_multiplier", 1) == 1.0) {
            return;
        }
        SPAWNER_MOBS.add(entity.getUUID());
    }

    public static void clear(Entity entity) {
        SPAWNER_MOBS.remove(entity.getUUID());
    }

    public static boolean isSpawnerSpawned(Entity entity) {
        return SPAWNER_MOBS.contains(entity.getUUID());
    }

    /**
     * XP multiplier for entity sources, matching Bukkit {@code getSpawnerMultiplier}.
     */
    public static double xpMultiplier(LivingEntity victim, Skill skill) {
        if (!isSpawnerSpawned(victim)) {
            return 1.0;
        }
        return skill.optionDouble("spawner_multiplier", 1.0);
    }
}
