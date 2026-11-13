package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.source.SkillSource;
import dev.aurelium.auraskills.api.source.type.EntityXpSource;
import dev.aurelium.auraskills.common.config.Option;
import dev.aurelium.auraskills.common.user.User;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import java.util.Arrays;

public final class FabricEntityKillHandler {

    private FabricEntityKillHandler() {
    }

    public static void register(FabricAuraSkills plugin) {
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> FabricSpawnerMobTracker.clear(entity));

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            try {
                if (!(entity.level() instanceof ServerLevel) || entity.level().isClientSide()) {
                    return;
                }
                if (entity instanceof ServerPlayer) {
                    return;
                }
                Entity attacker = damageSource.getEntity();
                if (!(attacker instanceof ServerPlayer player)) {
                    return;
                }
                if (plugin.configBoolean(Option.DISABLE_IN_CREATIVE_MODE) && player.getAbilities().instabuild) {
                    return;
                }
                String path = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getPath();

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
                    User user = plugin.getUserManager().getUser(player.getUUID());
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
