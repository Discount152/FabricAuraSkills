package dev.aurelium.auraskills.fabric;

import dev.aurelium.auraskills.api.source.SkillSource;
import dev.aurelium.auraskills.api.source.type.DamageXpSource;
import dev.aurelium.auraskills.api.source.type.DamageXpSource.DamageCause;
import dev.aurelium.auraskills.api.source.type.EntityXpSource;
import dev.aurelium.auraskills.api.source.type.EntityXpSource.EntityDamagers;
import dev.aurelium.auraskills.api.source.type.EntityXpSource.EntityTriggers;
import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.skill.Skills;
import dev.aurelium.auraskills.common.config.Option;
import dev.aurelium.auraskills.common.source.SourceTypes;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.common.util.data.Pair;
import dev.aurelium.auraskills.fabric.ability.FabricBleedListener;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Player {@link DamageXpSource} (defense, agility fall, etc.) — Bukkit {@code DamageListener} + {@code DamageLeveler}
 * — and {@link EntityXpSource} with {@link EntityTriggers#DAMAGE} when configured. Entity-DAMAGE path matches
 * {@code EntityLeveler#resolveDamager} and {@code FabricEntityXpSourceLookup#forEntityDamageOnly}.
 */
public final class FabricCombatXpHandler {

    private final FabricAuraSkills plugin;
    private final ConcurrentHashMap<UUID, Long> playerDamageCooldownUntil = new ConcurrentHashMap<>();

    private FabricCombatXpHandler(FabricAuraSkills plugin) {
        this.plugin = plugin;
    }

    public static void register(FabricAuraSkills plugin) {
        FabricCombatXpHandler handler = new FabricCombatXpHandler(plugin);
        ServerLivingEntityEvents.AFTER_DAMAGE.register(handler::onAfterDamage);
    }

    private void onAfterDamage(LivingEntity entity, DamageSource source, float baseDamageTaken, float damageTaken, boolean blocked) {
        if (!(entity.getEntityWorld() instanceof ServerWorld world) || world.isClient()) {
            return;
        }
        if (entity instanceof ServerPlayerEntity player) {
            handlePlayerDamaged(player, source, baseDamageTaken, damageTaken);
        } else {
            handleNonPlayerDamaged(entity, source, damageTaken);
        }
    }

    /**
     * Order aligned with Bukkit {@code DamageLeveler#onDamage}: source → cooldown → self-inflict → (implicit checks)
     * → shield (defense) → damage values → must survive → min/max.
     * Shield: reference uses {@link ServerPlayerEntity#isBlocking()} only, not a separate "blocked" flag.
     */
    private void handlePlayerDamaged(ServerPlayerEntity player, DamageSource source, float baseDamageTaken, float damageTaken) {
        if (!plugin.getSkillManager().isSourceEnabled(SourceTypes.DAMAGE)) {
            return;
        }
        DamageCause skillsCause = FabricDamageCauseMapper.toApiCause(source);
        SkillSource<DamageXpSource> skillSource = findDamageXpSource(source, skillsCause);
        if (skillSource == null) {
            return;
        }
        DamageXpSource dx = skillSource.source();
        Skill skill = skillSource.skill();
        if (FabricXpHandlerUtil.shouldBlockXp(plugin, player, skill, SourceTypes.DAMAGE)) {
            return;
        }
        long last = playerDamageCooldownUntil.getOrDefault(player.getUuid(), 0L);
        int cd = dx.getCooldownMs();
        if (cd > 0 && last + cd > System.currentTimeMillis()) {
            return;
        }
        if (isSelfInflicted(source, player)) {
            return;
        }
        if (skill == Skills.DEFENSE && !Skills.DEFENSE.optionBoolean("allow_shield_blocking", false) && player.isBlocking()) {
            return;
        }
        double dmg = dx.useOriginalDamage() ? baseDamageTaken : damageTaken;
        if (dmg <= 0) {
            return;
        }
        if (dx.mustSurvive() && damageTaken >= player.getHealth()) {
            return;
        }
        double xp = dmg * dx.getXp();
        if (skill == Skills.DEFENSE) {
            xp = Math.min(xp, Skills.DEFENSE.optionDouble("max", 100.0));
            if (xp < Skills.DEFENSE.optionDouble("min", 0)) {
                return;
            }
        }
        User user = plugin.getUserManager().getUser(player.getUuid());
        plugin.getLevelManager().addXp(user, skill, dx, xp);
        playerDamageCooldownUntil.put(player.getUuid(), System.currentTimeMillis());
    }

    private void handleNonPlayerDamaged(LivingEntity victim, DamageSource source, float damageTaken) {
        if (!plugin.getSkillManager().isSourceEnabled(SourceTypes.ENTITY)) {
            return;
        }
        if (!hasAnyEntityDamageTrigger()) {
            return;
        }
        if (victim instanceof ArmorStandEntity || damageTaken <= 0f) {
            return;
        }
        UUID bleedUser = FabricBleedListener.peekBleedAttackerUuid();
        Pair<ServerPlayerEntity, EntityDamagers> resolved = FabricEntityDamageAttribution.resolveForEntityLevelerDamage(source, plugin);
        ServerPlayerEntity player = resolved != null ? resolved.first() : null;
        EntityDamagers eventDamager = resolved != null ? resolved.second() : null;
        if (player == null && bleedUser != null) {
            player = plugin.getServer().getPlayerManager().getPlayer(bleedUser);
            eventDamager = EntityDamagers.PLAYER;
        }
        if (player == null || eventDamager == null) {
            return;
        }
        if (plugin.configBoolean(Option.DISABLE_IN_CREATIVE_MODE) && player.getAbilities().creativeMode) {
            return;
        }
        DamageCause skillsCause = FabricDamageCauseMapper.toApiCause(source);
        SkillSource<EntityXpSource> skillSource = FabricEntityXpSourceLookup.forEntityDamageOnly(
                plugin, victim, eventDamager, skillsCause);
        if (skillSource == null) {
            return;
        }
        EntityXpSource ex = skillSource.source();
        Skill skill = skillSource.skill();
        if (!skill.isEnabled()) {
            return;
        }
        if (FabricXpHandlerUtil.shouldBlockXp(plugin, player, skill, SourceTypes.ENTITY)) {
            return;
        }
        double xpPortion = damageTaken;
        if (ex.scaleXpWithHealth()) {
            EntityAttributeInstance max = victim.getAttributeInstance(EntityAttributes.MAX_HEALTH);
            if (max != null && max.getValue() > 0) {
                xpPortion = damageTaken / max.getValue();
            }
        }
        double xp = xpPortion * ex.getXp() * FabricSpawnerMobTracker.xpMultiplier(victim, skill);
        User user = plugin.getUserManager().getUser(player.getUuid());
        plugin.getLevelManager().addXp(user, skill, ex, xp);
    }

    private boolean hasAnyEntityDamageTrigger() {
        for (SkillSource<EntityXpSource> entry : plugin.getSkillManager().getSourcesOfType(EntityXpSource.class)) {
            for (EntityTriggers t : entry.source().getTriggers()) {
                if (t == EntityTriggers.DAMAGE) {
                    return true;
                }
            }
        }
        return false;
    }

    private SkillSource<DamageXpSource> findDamageXpSource(DamageSource source, DamageCause skillsCause) {
        for (SkillSource<DamageXpSource> entry : plugin.getSkillManager().getSourcesOfType(DamageXpSource.class)) {
            DamageXpSource s = entry.source();
            if (!causesMatch(s, skillsCause)) {
                continue;
            }
            if (excludedCauseMatches(s, skillsCause)) {
                continue;
            }
            if (!excludedDamagersMatch(s, source)) {
                continue;
            }
            if (!damagersMatch(s, source)) {
                continue;
            }
            return entry;
        }
        return null;
    }

    private static boolean causesMatch(DamageXpSource source, DamageCause skillsCause) {
        DamageCause[] causes = source.getCauses();
        if (causes == null) {
            return true;
        }
        for (DamageCause c : causes) {
            if (c.toString().equals(skillsCause.toString())) {
                return true;
            }
        }
        return false;
    }

    private static boolean excludedCauseMatches(DamageXpSource source, DamageCause skillsCause) {
        DamageCause[] excluded = source.getExcludedCauses();
        if (excluded == null) {
            return false;
        }
        for (DamageCause c : excluded) {
            if (c.toString().equals(skillsCause.toString())) {
                return true;
            }
        }
        return false;
    }

    private boolean excludedDamagersMatch(DamageXpSource source, DamageSource mcSource) {
        String[] excluded = source.getExcludedDamagers();
        if (excluded == null) {
            return true;
        }
        Entity damager = getDirectDamager(mcSource);
        if (damager == null) {
            return false;
        }
        for (String name : excluded) {
            if (nameMatchesDamageDamager(source, damager, name)) {
                return false;
            }
        }
        return true;
    }

    private boolean damagersMatch(DamageXpSource source, DamageSource mcSource) {
        String[] damagers = source.getDamagers();
        if (damagers == null) {
            return true;
        }
        Entity damager = getDirectDamager(mcSource);
        if (damager == null) {
            return false;
        }
        for (String name : damagers) {
            if (nameMatchesDamageDamager(source, damager, name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Bukkit {@code DamageLeveler#damagerMatches} / {@code includeProjectiles} for {@link Projectile} including
     * potions: vanilla uses {@link ThrownItemEntity} for splash potion, not always {@link ProjectileEntity}.
     */
    private boolean nameMatchesDamageDamager(DamageXpSource source, Entity damager, String name) {
        if (source.includeProjectiles() && damager instanceof ProjectileEntity projectile) {
            return projectileSourceMatches(projectile.getOwner(), name);
        }
        if (source.includeProjectiles() && damager instanceof ThrownItemEntity thrown) {
            return projectileSourceMatches(thrown.getOwner(), name);
        }
        return entityDamagerMatches(damager, name);
    }

    private static boolean entityDamagerMatches(Entity damager, String name) {
        if ("mob".equalsIgnoreCase(name)) {
            return damager instanceof MobEntity;
        }
        if ("player".equalsIgnoreCase(name)) {
            return damager instanceof PlayerEntity;
        }
        String path = Registries.ENTITY_TYPE.getId(damager.getType()).getPath();
        return path.equalsIgnoreCase(name);
    }

    private static boolean projectileSourceMatches(Entity projectileSource, String name) {
        if (projectileSource == null) {
            return false;
        }
        if ("mob".equalsIgnoreCase(name)) {
            return projectileSource instanceof MobEntity;
        }
        if ("player".equalsIgnoreCase(name)) {
            return projectileSource instanceof PlayerEntity;
        }
        if (projectileSource instanceof LivingEntity living) {
            return Registries.ENTITY_TYPE.getId(living.getType()).getPath().equalsIgnoreCase(name);
        }
        return false;
    }

    private static Entity getDirectDamager(DamageSource source) {
        Entity s = source.getSource();
        if (s != null) {
            return s;
        }
        return source.getAttacker();
    }

    private static boolean isSelfInflicted(DamageSource source, ServerPlayerEntity victim) {
        Entity attacker = source.getAttacker();
        if (attacker instanceof PlayerEntity p && p.getUuid().equals(victim.getUuid())) {
            return true;
        }
        Entity direct = source.getSource();
        if (direct instanceof ProjectileEntity proj) {
            Entity owner = proj.getOwner();
            if (owner != null && owner.getUuid().equals(victim.getUuid())) {
                return true;
            }
        }
        if (direct instanceof ThrownItemEntity thrown) {
            Entity owner = thrown.getOwner();
            if (owner != null && owner.getUuid().equals(victim.getUuid())) {
                return true;
            }
        }
        return false;
    }
}
