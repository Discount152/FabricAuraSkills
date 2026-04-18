package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.source.SkillSource;
import dev.aurelium.auraskills.api.source.type.EntityXpSource;
import dev.aurelium.auraskills.common.config.Option;
import dev.aurelium.auraskills.common.user.User;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.Arrays;

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
                Entity attacker = damageSource.getAttacker();
                if (!(attacker instanceof ServerPlayerEntity player)) {
                    return;
                }
                if (plugin.configBoolean(Option.DISABLE_IN_CREATIVE_MODE) && player.getAbilities().creativeMode) {
                    return;
                }
                String path = Registries.ENTITY_TYPE.getId(entity.getType()).getPath();

                for (SkillSource<EntityXpSource> entry : plugin.getSkillManager().getSourcesOfType(EntityXpSource.class)) {
                    EntityXpSource source = entry.source();
                    if (!Arrays.asList(source.getTriggers()).contains(EntityXpSource.EntityTriggers.DEATH)) {
                        continue;
                    }
                    if (!Arrays.asList(source.getDamagers()).contains(EntityXpSource.EntityDamagers.PLAYER)) {
                        continue;
                    }
                    if (!source.getEntity().equalsIgnoreCase(path)) {
                        continue;
                    }
                    Skill skill = entry.skill();
                    if (!skill.isEnabled()) {
                        continue;
                    }
                    User user = plugin.getUserManager().getUser(player.getUuid());
                    if (!user.hasSkillPermission(skill)) {
                        continue;
                    }
                    String worldName = FabricUser.worldNameCompat(player);
                    if (plugin.getWorldManager().isBlockedWorld(worldName) || plugin.getWorldManager().isDisabledWorld(worldName)) {
                        continue;
                    }
                    double xp = FabricSpawnerMobTracker.xpMultiplier(entity, skill) * source.getXp();
                    plugin.getLevelManager().addXp(user, skill, source, xp);
                    return;
                }
            } finally {
                FabricSpawnerMobTracker.clear(entity);
            }
        });
    }
}
