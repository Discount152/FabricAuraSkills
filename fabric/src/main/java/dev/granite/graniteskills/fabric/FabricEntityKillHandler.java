package dev.granite.graniteskills.fabric;

import dev.granite.graniteskills.api.skill.Skill;
import dev.granite.graniteskills.api.source.SkillSource;
import dev.granite.graniteskills.api.source.type.EntityXpSource;
import dev.granite.graniteskills.common.config.Option;
import dev.granite.graniteskills.common.user.User;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.throwableitemprojectile.AbstractThrownPotion;

import java.util.Arrays;
import java.util.UUID;

public final class FabricEntityKillHandler {

    private FabricEntityKillHandler() {
    }

    public static void register(FabricGraniteSkills plugin) {
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> FabricSpawnerMobTracker.clear(entity));

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            try {
                if (!(entity.level() instanceof ServerLevel) || entity.level().isClientSide()) {
                    return;
                }
                if (entity instanceof ServerPlayer) {
                    return;
                }
                Entity direct = damageSource.getDirectEntity();
                Entity causing = damageSource.getEntity();
                boolean projectile = direct instanceof Projectile;
                UUID owner = null;
                if (projectile && ((Projectile) direct).getOwner() instanceof ServerPlayer ownerPlayer) {
                    owner = ownerPlayer.getUUID();
                }
                UUID causingPlayer = causing instanceof ServerPlayer causingAsPlayer ? causingAsPlayer.getUUID() : null;
                UUID directPlayer = direct instanceof ServerPlayer directAsPlayer ? directAsPlayer.getUUID() : null;
                ProjectileKillCredit.Credit credit = ProjectileKillCredit.resolve(
                        projectile,
                        direct instanceof AbstractThrownPotion,
                        owner,
                        causingPlayer,
                        directPlayer);
                if (credit == null) {
                    return;
                }
                ServerPlayer player = plugin.getServer().getPlayerList().getPlayer(credit.playerId());
                if (player == null) {
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
                    if (source.getDamagers() == null || !Arrays.asList(source.getDamagers()).contains(credit.damager())) {
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
                    FabricXpContext.run(entity.getUUID(), () -> plugin.getLevelManager().addXp(user, skill, source, xp));
                    if (entity.level() instanceof ServerLevel lootLevel) {
                        dev.granite.graniteskills.fabric.loot.FabricSkillLoot.tryRoll(
                                plugin, player, lootLevel, entity.blockPosition(), user, skill, source);
                    }
                    dev.granite.graniteskills.fabric.ability.FabricAlchemyAbilities.onKill(plugin, player, entity);
                    if (entity.level() instanceof ServerLevel level) {
                        dev.granite.graniteskills.fabric.ability.FabricEnchantingAbilities.onKill(
                                plugin, player, entity.getExperienceReward(level, player));
                    }
                    return;
                }
            } finally {
                FabricSpawnerMobTracker.clear(entity);
            }
        });
    }
}
