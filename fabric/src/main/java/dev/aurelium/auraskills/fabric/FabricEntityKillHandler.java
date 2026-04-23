package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.source.SkillSource;
import dev.aurelium.auraskills.api.source.type.DamageXpSource.DamageCause;
import dev.aurelium.auraskills.api.source.type.EntityXpSource;
import dev.aurelium.auraskills.api.source.type.EntityXpSource.EntityDamagers;
import dev.aurelium.auraskills.common.source.SourceTypes;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

/**
 * Bukkit {@code EntityLeveler#onEntityDeath} — awards {@link EntityXpSource} with {@code DEATH} trigger and the
 * last-hit {@link DamageCause} (and player / player-owned projectile, or bleed) via {@link FabricEntityXpSourceLookup#forEntityDeathKill}.
 */
public final class FabricEntityKillHandler {

    private FabricEntityKillHandler() {
    }

    public static void register(FabricAuraSkills plugin) {
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> FabricSpawnerMobTracker.clear(entity));

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            try {
                if (!(entity.getEntityWorld() instanceof ServerWorld) || entity.getEntityWorld().isClient()) {
                    return;
                }
                if (entity instanceof ServerPlayerEntity) {
                    return;
                }
                if (!plugin.getSkillManager().isSourceEnabled(SourceTypes.ENTITY)) {
                    return;
                }
                var killer = FabricEntityDamageAttribution.resolveKillerOnDeath(
                        damageSource, entity, entity.getEntityWorld().getServer());
                if (killer == null) {
                    return;
                }
                ServerPlayerEntity player = killer.first();
                EntityDamagers eventDamager = killer.second();
                DamageCause skillsCause = FabricDamageCauseMapper.toApiCause(damageSource);
                SkillSource<EntityXpSource> skillSource = FabricEntityXpSourceLookup.forEntityDeathKill(
                        plugin, entity, eventDamager, skillsCause);
                if (skillSource == null) {
                    return;
                }
                EntityXpSource source = skillSource.source();
                var skill = skillSource.skill();
                if (FabricXpHandlerUtil.shouldBlockXp(plugin, player, skill, SourceTypes.ENTITY)) {
                    return;
                }
                double xp = FabricSpawnerMobTracker.xpMultiplier(entity, skill) * source.getXp();
                var user = plugin.getUserManager().getUser(player.getUuid());
                plugin.getLevelManager().addXp(user, skill, source, xp);
            } finally {
                FabricSpawnerMobTracker.clear(entity);
            }
        });
    }
}
